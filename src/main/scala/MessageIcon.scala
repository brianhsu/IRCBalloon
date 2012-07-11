package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom._

import org.eclipse.swt._
import scala.math._
import scala.collection.JavaConversions._
import I18N.i18n._

trait MessageIcon
{
    def nicknameStyles(message: String, color: Color, font: Font): List[StyleRange] = 
    {
        val regex = """\w+:""".r

        regex.findAllIn(message).matchData.map { data => 
            val style = new StyleRange
            style.start = data.start
            style.length = data.end - data.start
            style.foreground = color
            style.font = font
            style
        }.toList
    }

    def opStyles(message: String): List[StyleRange] = 
    {
        val regex = """\[OP\] """.r

        regex.findAllIn(message).matchData.map { data => 
            val style = new StyleRange

            style.start = data.start
            style.length = data.end - data.start
            style.data = MyIcon.ircOP
            style.metrics = new GlyphMetrics(
                MyIcon.ircOP.getBounds.height, 0, 
                MyIcon.ircOP.getBounds.width / 4
            )

            style
        }.toList
    }

    def emoteStyles(message: String): List[StyleRange] = {
        
        val styleList = Emotes.getEmotes.map { case (text, image) =>
            val regex = """\Q%s\E""".format(text).r

            regex.findAllIn(message).matchData.map { data => 
                val style = new StyleRange
                style.start = data.start
                style.length = data.end - data.start
                style.data = image
                style.metrics = new GlyphMetrics(
                    MyIcon.ircOP.getBounds.height, 0, 
                    MyIcon.ircOP.getBounds.width / text.length
                )

                style
            }.toList
        }

        styleList.flatten.toList
    }
}

