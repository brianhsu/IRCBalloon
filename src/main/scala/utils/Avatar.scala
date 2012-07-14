package org.bone.ircballoon

import org.eclipse.swt.widgets._
import org.eclipse.swt.graphics._

object Avatar
{
    import scala.xml.XML
    import scala.io.Source
    import java.net.URL

    var displayAvatar: Boolean = true
    var onlyAvatar: Boolean = false
    var usingTwitchAvatar: Boolean = false
    var usingTwitchNickname: Boolean = true

    private var twitchNicknames: Map[String, Option[String]] = Map()
    private var twitchAvatars: Map[String, Option[Image]] = Map()
    private var customAvatars: Map[String, (String, Image)] = Map()

    def getCustomAvatars = customAvatars

    def getTwitchNickname(nickname: String): Option[String] = {
        try {

            val profileURL = "http://api.justin.tv/api/user/show/" + nickname + ".xml"
            val twitchUserXML = XML.loadString(Source.fromURL(profileURL).mkString)
            val twitchNickname = (twitchUserXML \\ "name").map(_.text).filterNot(_.isEmpty)

            twitchNickname match {
                case text :: Nil => Some(text)
                case _ => None
            }

        } catch {
            case e => None
        }
    }

    def getTwitchNicknameCache(nickname: String): Option[String] = {
        
        twitchNicknames.get(nickname) match {
            case Some(twitchNickname) => twitchNickname
            case None => 
                val twitchNickname = getTwitchNickname(nickname)
                twitchNicknames += (nickname -> twitchNickname)
                twitchNickname
        }

    }

    def getTwitchAvatar(nickname: String): Option[Image] = {

        def isDefault(url: String) = url.contains("404_user")
        def getImage(url: String) = {
            val inputStream = new URL(url).openStream
            val image = new Image(Display.getDefault, inputStream)
            inputStream.close()
            Some(image)
        }

        try {
            val profileURL = "http://api.justin.tv/api/user/show/" + nickname + ".xml"
            val twitchUserXML = XML.loadString(Source.fromURL(profileURL).mkString)
            val imageURLTiny = 
                (twitchUserXML \\ "image_url_tiny").map(_.text).filterNot(isDefault)

            val avatarImage =  imageURLTiny match {
                case imageURL :: Nil => getImage(imageURL)
                case _ => None
            }

            avatarImage

        } catch {
            case _ => None
        }
    }

    def getTwitchAvatarCache(nickname: String): Option[Image] = {
        
        twitchAvatars.get(nickname) match {
            case Some(avatarOption) => avatarOption
            case None => 
                val avatarOption = getTwitchAvatar(nickname)
                twitchAvatars += (nickname -> avatarOption)
                avatarOption
        }

    }

    def getImageFile(filePath: String) =
    {
        new Image(Display.getDefault, filePath)
    }

    def addAvatar(nickname: String, imagePath: String)
    {
        customAvatars += (nickname -> (imagePath, getImageFile(imagePath)))
    }

    def removeAvatar(nickname: String)
    {
        customAvatars.get(nickname).foreach(_._2.dispose())
        customAvatars -= nickname
    }

    def apply(nickname: String): Option[Image] = usingTwitchAvatar match {  
        case true  => customAvatars.get(nickname).map(_._2) orElse 
                      getTwitchAvatarCache(nickname)

        case false => customAvatars.get(nickname).map(_._2)
    }

}
