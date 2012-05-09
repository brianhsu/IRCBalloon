package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText

import org.eclipse.swt._
import scala.math._

trait NotificationBalloon
{
    this: BalloonController =>

    trait BalloonTheme {
        this: BalloonWindow =>
    
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

    object BalloonWindow
    {
        private var sn: Int = 0
        private def createSN() = {
            sn = sn + 1
            sn
        }
    }

    case class BalloonWindow(message: String, width: Int = 200) extends BalloonTheme
    {
        val sn = BalloonWindow.createSN()
        val locationX = 100
        val uid = System.identityHashCode(this)
        val display = Display.getDefault
        val shell = new Shell(display, SWT.NO_TRIM|SWT.ON_TOP|SWT.RESIZE)
        val label = new StyledText(shell, SWT.MULTI|SWT.READ_ONLY|SWT.WRAP|SWT.NO_FOCUS)

    
        def windowHeight = shell.isDisposed match {
            case true  => 0
            case fasle => shell.getSize.y
        }
    
        var bottomY: Int = 0
        /*
        def bottomY = shell.isDisposed match {
            case true  => 0
            case false => shell.getLocation.y + shell.getSize.y
        }
        */
    
        def calculateSize() =
        {
            val labelSize = label.computeSize(width, SWT.DEFAULT, true)
            (labelSize.x, labelSize.y)
        }
    
        def setupLayout()
        {
            val layout = new GridLayout(1, true)
            val layoutData = new GridData(SWT.FILL, SWT.CENTER, true, true)
            layout.marginTop = 6
            shell.setLayout(layout)
            label.setLayoutData(layoutData)
            label.setForeground(MyColor.White)
            label.setLineSpacing(5)
            label.setEnabled(false)
            label.setText(message)
            val (width, height) = calculateSize()
            shell.setSize(width, height + 20)
            shell.setLocation(locationX, calculateLocationY)
            bottomY = calculateLocationY + height + 20
        }
    
    
        def open()
        {
            setupBackground()
            setupLayout()

            addNotification(this)
   
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
                        removeNotification(BalloonWindow.this)
                    }
                }
            }
        }
    
        def fadeIn()
        {
            Display.getDefault.timerExec(FadeTick, new FadeIn)
        }
    }
}
