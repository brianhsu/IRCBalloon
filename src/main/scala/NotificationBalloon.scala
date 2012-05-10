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

    val FadeStep = 5
    val FadeTick = fadeTime / (alpha / 5)

    case class BalloonWindow(bgColor: Color, borderColor: Color, 
                             message: String) extends NotificationTheme with NotificationWindow
    {
        val display = Display.getDefault

        val uid = System.identityHashCode(this)
        val shell = new Shell(display, SWT.NO_TRIM|SWT.ON_TOP|SWT.RESIZE)
        val label = new StyledText(shell, SWT.MULTI|SWT.READ_ONLY|SWT.WRAP|SWT.NO_FOCUS)
        var bottomY: Int = 0
    
        def calculateSize() =
        {
            val labelSize = label.computeSize(size._1, SWT.DEFAULT, true)
            (labelSize.x, labelSize.y)
        }
    
        def setLayout()
        {
            val layout = new GridLayout(1, true)
            val layoutData = new GridData(SWT.FILL, SWT.CENTER, true, true)
            layout.marginTop = 6
            shell.setLayout(layout)
            label.setLayoutData(layoutData)
            label.setForeground(fontColor)
            label.setFont(font)
            label.setLineSpacing(5)
            label.setEnabled(false)
            label.setText(message)
            val (width, height) = calculateSize()
            shell.setSize(width, height + 20)
            shell.setLocation(location._1, calculateLocationY)
            bottomY = calculateLocationY + height + 20
        }
    
    
        def open()
        {
            setBackground()
            setLayout()

            addNotification(this)
   
            shell.setAlpha(0)
            shell.open()
            fadeIn()
        }
        
        class FadeIn extends Runnable
        {
            override def run() 
            {
                if (shell.isDisposed) { return }
    
                val nextAlpha = min(255, shell.getAlpha + FadeStep)
    
                shell.setAlpha(nextAlpha)
    
                if (nextAlpha < alpha) {
                    Display.getDefault.timerExec(FadeTick, this)
                } else {
                    Display.getDefault.timerExec(displayTime, new FadeOut())
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
