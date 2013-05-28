package org.bone.ircballoon.model

import org.eclipse.swt.widgets._
import org.eclipse.swt.graphics._

import org.bone.ircballoon._
import org.bone.ircballoon.ImageUtil._

import scala.xml.XML
import scala.io.Source
import scala.util.control.Exception._

object TwitchUser
{
  case class CustomAvatar(file: String, image: Image)

  private var customAvatars: Map[String, CustomAvatar] = Map()
  private var userCache: Map[String, TwitchUser] = Map()

  def getAvatars = customAvatars

  def addAvatar(nickname: String, imageFile: String)
  {
    loadFromFile(imageFile).foreach { image =>
      val avatar = CustomAvatar(imageFile, image)
      customAvatars += (nickname -> avatar)
    }
  }

  def removeAvatar(nickname: String)
  {
    customAvatars.get(nickname).foreach(_.image.dispose())
    customAvatars -= nickname
  }

  def apply(username: String): TwitchUser = {
    userCache.get(username) match {
      case Some(user) => user
      case None =>
        val user = new TwitchUser(username)
        userCache += (username -> user)
        user
    }
  }
}

class TwitchUser(val username: String)
{
  lazy val twitchAvatar = getTwitchAvatar(username)

  private def getTwitchAvatar(nickname: String): Option[Image] = allCatch.opt 
  {
    def isDefault(url: String) = url.contains("404_user")

    val profileURL = "http://api.justin.tv/api/user/show/" + nickname + ".xml"
    val twitchUserXML = XML.loadString(Source.fromURL(profileURL).mkString)
    val imageURLTiny = (twitchUserXML \\ "image_url_tiny").map(_.text).filterNot(isDefault)
    val imageURL = imageURLTiny(0)

    loadFromURL(imageURL).map(image => resize(image, (36, 36))).get
  }

  def avatar: Option[Image] = {

    val customAvatar = TwitchUser.customAvatars.get(username).map(_.image)

    Preference.usingTwitchAvatar match {  
      case true  => customAvatar orElse twitchAvatar
      case false => customAvatar
    }
  }
}

