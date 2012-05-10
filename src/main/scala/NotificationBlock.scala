package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText

import org.eclipse.swt._
import scala.math._

trait BlockTheme {
    this: NotificationBlock =>

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

case class NotificationBlock(size: (Int, Int), location: (Int, Int), 
                             borderColor: Color, backgroundColor: Color, alpha: Int,
                             fontColor: Color, font: Font, 
                             messageSize: Int) extends Notification with BlockTheme with SWTHelper
{
    val display = Display.getDefault
    val shell = new Shell(display, SWT.NO_TRIM|SWT.ON_TOP|SWT.RESIZE)
    val label = createContentLabel()
    var messages: List[String] = Nil
    val (inputLabel, inputText) = createChatInputBox()

    def createChatInputBox() = 
    {
        val label = new Label(shell, SWT.LEFT)
        val text = new Text(shell, SWT.BORDER)

        label.setText("聊天：")
        label.setForeground(fontColor)
        
        text.setBackground(backgroundColor)
        text.setForeground(fontColor)
        text.setFont(font)
        text.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false))
        text.addTraverseListener(new TraverseListener() {
            override def keyTraversed(e: TraverseEvent) {
                if (e.detail == SWT.TRAVERSE_RETURN && text.getText.trim.length > 0) {
                    val message = text.getText.trim()
                    val displayMessage = "%s:%s" format(MainWindow.getNickname, message)

                    MainWindow.getIRCBot.foreach { bot => 
                        bot.getChannels.foreach { channel =>
                            bot.sendMessage(channel, message)
                        }
                    }

                    NotificationBlock.this.addMessage(displayMessage)
                    text.setText("")
                }
            }
        })
        (label, text)
    }

    def createContentLabel() = 
    {
        val layoutData = new GridData(SWT.FILL, SWT.FILL, true, true)
        val label = new StyledText(shell, SWT.MULTI|SWT.READ_ONLY|SWT.WRAP|SWT.NO_FOCUS)

        layoutData.horizontalSpan = 2
        label.setLayoutData(layoutData)

        label
    }

    override def onTrayIconClicked(): Unit =
    {
        shell.setVisible(!shell.isVisible)
    }

    def addMessage(newMessage: String)
    {
        messages = (newMessage :: messages).take(messageSize)
        updateMessages()
    }
    
    def updateMessages()
    {
        display.syncExec (new Runnable {
            override def run () {
                if (!shell.isDisposed) {
                    label.setText(messages.take(messageSize).reverse.mkString("\n"))
                }
            }
        })
    }

    def this()
    {
        this(
            (300, 448), (100, 100), 
            MyColor.White, MyColor.Black, 210, 
            MyColor.White, MyFont.Default, 10
        )
    }

    def setupLayout()
    {
        val layout = new GridLayout(2, false)
        shell.setLayout(layout)
        label.setFont(font)
        label.setForeground(fontColor)
        label.setLineSpacing(5)
        label.setEnabled(false)
    }

    def open()
    {
        setupBackground()
        setupLayout()

        shell.setAlpha(alpha)
        shell.setSize(size._1, size._2)
        shell.setLocation(location._1, location._2)
        shell.open()
        updateMessages()
    }

    def close()
    {
        if (!shell.isDisposed) {
            shell.close()
        }
    }

}

