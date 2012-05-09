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

object NotificationBalloon
{
    var currentNotification: List[NotificationBalloon] = Nil

    def count = currentNotification.size
    var isFull = false

    def addNotification(notification: NotificationBalloon)
    {
        currentNotification = notification :: currentNotification
        isFull = isFull || currentNotification.size >= 5
        println("isFull:" + isFull)
    }

    def removeNotification(finished: NotificationBalloon) {
        currentNotification = currentNotification.filterNot(_.uid == finished.uid)
        finished.shell.dispose()

        if (currentNotification.size == 0) {
            isFull = false
        }
    }

    def calculateLocationY = {
        currentNotification.map(_.bottomY) match {
            case Nil => 100
            case xs  => xs.max + 10
        }
    }

}

case class NotificationBalloon(message: String, width: Int = 200) extends BalloonTheme
{
    val locationX = 100
    val uid = System.identityHashCode(this)
    val display = Display.getDefault
    val shell = new Shell(display, SWT.NO_TRIM|SWT.ON_TOP|SWT.RESIZE)
    val label = new StyledText(shell, SWT.MULTI|SWT.READ_ONLY|SWT.WRAP|SWT.NO_FOCUS)

    def windowHeight = shell.isDisposed match {
        case true  => 0
        case fasle => shell.getSize.y
    }

    override def toString = "[%d] %s" format(uid, message)

    def bottomY = shell.isDisposed match {
        case true  => 0
        case false => shell.getLocation.y + shell.getSize.y
    }

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
        shell.setLocation(locationX, NotificationBalloon.calculateLocationY)
        NotificationBalloon.addNotification(this)
    }


    def open()
    {
        setupBackground()
        setupLayout()

        shell.setAlpha(0)
        shell.open()
        fadeIn()
    }

    val FadeStep = 5
    val FadeTick = 25
    val DisplayTime = 5000
    val MaxAlpha = 200

    class FadeIn extends Runnable
    {
        override def run() 
        {
            if (shell.isDisposed) { return }

            val nextAlpha = min(255, shell.getAlpha + FadeStep)

            shell.setAlpha(nextAlpha)

            if (nextAlpha < MaxAlpha) {
                Display.getDefault.timerExec(FadeTick, this)
            } else {
                Display.getDefault.timerExec(DisplayTime, new FadeOut())
            }
        }
    }

    class FadeOut extends Runnable
    {
        override def run() {

            if (!shell.isDisposed) {
                val nextAlpha = max(0, shell.getAlpha - FadeStep)

                shell.setAlpha(nextAlpha)

                if (nextAlpha > 0) {
                    Display.getDefault.timerExec(FadeTick, this)
                } else {
                    NotificationBalloon.removeNotification(NotificationBalloon.this)
                }
            }
        }
    }

    def fadeIn()
    {
        Display.getDefault.timerExec(FadeTick, new FadeIn)
    }
}

class BalloonControler extends Notification
{
    def addMessage(message: String)
    {
        val thread = new Thread() {
            override def run() {
                
                while (NotificationBalloon.isFull) {
                    println("睡 0.5 秒")
                    Thread.sleep(500)
                }

                Display.getDefault.syncExec(new Runnable() {
                    override def run() {
                        val balloon = new NotificationBalloon(message)
                        balloon.open()
                    }
                })
            }
        }

        thread.start()
    }

    def open()
    {
    }

    def close()
    {
    }
}

