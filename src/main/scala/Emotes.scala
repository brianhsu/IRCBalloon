package org.bone.ircballoon

import org.eclipse.swt.widgets._
import org.eclipse.swt.graphics._

object Emotes
{
    def getImage(path: String) = {
        new Image(Display.getDefault, getClass().getResourceAsStream(path))
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
