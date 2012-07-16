package org.bone.ircballoon

import org.eclipse.swt.widgets._
import org.eclipse.swt.graphics._

import ImageUtil._

object Emotes
{
    var useDefault: Boolean = true

    private var customEmotes: Map[String, String] = Map()
    private var customIcons: Map[String, Image] = Map()

    def getCustomEmotes = customEmotes

    def addEmote(emotes: EmoteIcon)
    {
        customEmotes = customEmotes.updated(emotes.targetText, emotes.imagePath)
        customIcons = customIcons.updated(
            emotes.targetText,
            loadFromFile(emotes.imagePath).get
        )
    }

    def removeEmote(targetText: String)
    {
        customEmotes -= targetText
        customIcons.get(targetText).foreach(_.dispose())
        customIcons -= targetText
    }

    def getEmotes: Map[String, Image] = {
        
        useDefault match {
            case true => default ++ customIcons
            case false => customIcons
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
