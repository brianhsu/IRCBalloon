package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText

import org.eclipse.swt._
import scala.math._

trait NotificationTheme {
    this: NotificationWindow =>

    val (startColor, endColor) = gradientColor
    var oldImage: Option[Image] = None

    protected def gradientColor = {

        val rgb = bgColor.getRGB

        val red = min(255, rgb.red + 50)
        val green = min(255, rgb.green + 50)
        val blue = min(255, rgb.blue + 50)

        val startColor = new Color(display, red, green, blue)
        val endColor = bgColor

        (startColor, endColor)
    }

    def setBackground()
    {
        shell.setBackgroundMode(SWT.INHERIT_DEFAULT)

        shell.addListener(SWT.Resize, new Listener() {

            def drawBackground(rect: Rectangle, gc: GC)
            {
                gc.setForeground(startColor)
                gc.setBackground(endColor)
                
                gc.fillGradientRectangle(rect.x, rect.y, rect.width, rect.height, true)
            }

            def drawBorder(rect: Rectangle, gc: GC)
            {
                gc.setLineWidth(2)
                gc.setForeground(borderColor)

                gc.drawRectangle(rect.x+1, rect.y+1, rect.width -2, rect.height-2)
            }

            override def handleEvent(e: Event) {
                val rect = shell.getClientArea
                val newImage = new Image(display, max(1, rect.width), rect.height)
                val gc = new GC(newImage)

                drawBackground(rect, gc)
                drawBorder(rect, gc)

                gc.dispose()

                shell.setBackgroundImage(newImage)

                oldImage.foreach(_.dispose)
                oldImage = Some(newImage)
            }
        })
    }
}

