package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText

import org.eclipse.swt._
import scala.math._

trait BalloonTheme {
    this: NotificationBalloon =>

    val backgroundColor = MyColor.Black
    val borderColor = MyColor.White

    val (startColor, endColor) = gradientColor
    var oldImage: Option[Image] = None

    protected def gradientColor = {

        val rgb = backgroundColor.getRGB

        val red = min(255, rgb.red + 50)
        val green = min(255, rgb.green + 50)
        val blue = min(255, rgb.blue + 50)

        val startColor = new Color(display, red, green, blue)
        val endColor = backgroundColor

        (startColor, endColor)
    }

    def setupBackground()
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

case class NotificationBalloon(message: String, width: Int = 200) extends BalloonTheme
{
    val display = Display.getDefault
    val shell = new Shell(display, SWT.NO_TRIM|SWT.ON_TOP|SWT.RESIZE)
    val label = new StyledText(shell, SWT.MULTI|SWT.READ_ONLY|SWT.WRAP|SWT.NO_FOCUS)

    def calculateSize() =
    {
        val labelSize = label.computeSize(width, SWT.DEFAULT, true)
        (labelSize.x, labelSize.y)
    }

    def setupLayout()
    {
        val layout = new GridLayout(1, true)
        val layoutData = new GridData(SWT.CENTER, SWT.CENTER, true, true)
        layout.marginTop = 6
        shell.setLayout(layout)
        label.setLayoutData(layoutData)
        label.setForeground(MyColor.White)
        label.setLineSpacing(5)
        label.setEnabled(false)
        label.setText(message)
        val (width, height) = calculateSize()
        shell.setSize(width, height + 20)
    }

    def open()
    {
        setupBackground()
        setupLayout()

        shell.setLocation(100, 100)
        shell.open()
    }

}

