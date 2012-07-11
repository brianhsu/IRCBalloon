package org.bone.ircballoon

import org.eclipse.swt.widgets._
import org.eclipse.swt.graphics._

object Emotes
{
    var useDefault: Boolean = true

    private var customEmotes: Map[String, String] = Map()
    private var customIcons: Map[String, Image] = Map()

    def getCustomEmotes = customEmotes

    def addEmote(emotes: EmoteIcon)
    {
        customEmotes = customEmotes.updated(emotes.targetText, emotes.imagePath)
        customIcons = customIcons.updated(emotes.targetText, getImageFile(emotes.imagePath))
    }

    def removeEmote(targetText: String)
    {
        customEmotes -= targetText
        customIcons.get(targetText).foreach(_.dispose())
        customIcons -= targetText
    }

    def getImageFile(filePath: String) =
    {
        new Image(Display.getDefault, filePath)
    }

    def getImage(path: String) = {
        new Image(Display.getDefault, getClass().getResourceAsStream(path))
    }

    def getEmotes: Map[String, Image] = {
        
        useDefault match {
            case true => default ++ customIcons
            case false => customIcons
        }
    }

    val default = Map(
        ":)" -> getImage("/emotes/face-smile.png"),
        ":D" -> getImage("/emotes/face-laugh.png"),
        ":o" -> getImage("/emotes/face-surprise.png"),
        ":(" -> getImage("/emotes/face-sad.png"),
        ":p" -> getImage("/emotes/face-raspberry.png"),
        "8)" -> getImage("/emotes/face-cool.png"),
        ":X" -> getImage("/emotes/face-angry.png"),
        ";)" -> getImage("/emotes/face-wink.png")
    )
}
