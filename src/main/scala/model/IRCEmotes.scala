package org.bone.ircballoon.model

import org.eclipse.swt.widgets._
import org.eclipse.swt.graphics._

import org.bone.ircballoon.ImageUtil._
import org.bone.ircballoon.Preference._

object IRCEmotes
{
  case class CustomEmote(file: String, image: Image)

  private var customEmotes: Map[String, CustomEmote] = Map()

  def getCustomEmotes = customEmotes

  def addEmote(text: String, imageFile: String)
  {
    loadFromFile(imageFile).foreach { image =>
      customEmotes += (text -> CustomEmote(imageFile, image))
    }
  }

  def removeEmote(text: String)
  {
    customEmotes.get(text).foreach(_.image.dispose())
    customEmotes -= text
  }

  def getEmotes: Map[String, Image] = {
      
    usingDefaultEmotes match {
      case true  => default ++ customEmotes.map{ case(key, value) => (key, value.image) }
      case false => customEmotes.map{ case(key, value) => (key, value.image) }
    }
  }

  val default = Map(
    ":)" -> loadFromResource("/emotes/face-smile.png").get,
    ":D" -> loadFromResource("/emotes/face-laugh.png").get,
    ":o" -> loadFromResource("/emotes/face-surprise.png").get,
    ":(" -> loadFromResource("/emotes/face-sad.png").get,
    ":p" -> loadFromResource("/emotes/face-raspberry.png").get,
    "8)" -> loadFromResource("/emotes/face-cool.png").get,
    ":X" -> loadFromResource("/emotes/face-angry.png").get,
    ";)" -> loadFromResource("/emotes/face-wink.png").get
  )
}
