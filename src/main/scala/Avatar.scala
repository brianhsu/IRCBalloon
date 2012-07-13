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

    private var twitchAvatars: Map[String, Option[Image]] = Map()

    def getTwitchAvatar(nickname: String): Option[Image] = {
        
        def getImage(url: String) = {
            try {
                val inputStream = new URL(url).openStream
                val image = new Image(Display.getDefault, inputStream)
                inputStream.close()
                Some(image)
            } catch {
                case e => None
            }
        }

        def isDefault(url: String) = url.contains("404_user")

        val profileURL = "http://api.justin.tv/api/user/show/" + nickname + ".xml"
        val twitchUserXML = XML.loadString(Source.fromURL(profileURL).mkString)
        val imageURLTiny = (twitchUserXML \\ "image_url_tiny").map(_.text).filterNot(isDefault)
        val avatarImage =  imageURLTiny match {
            case imageURL :: Nil => getImage(imageURL)
            case _ => None
        }

        avatarImage
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

    def apply(nickname: String): Option[Image] = None

}
