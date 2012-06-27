package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StyleRange

import org.eclipse.swt._
import scala.math._
import scala.collection.JavaConversions._

case class NotificationBlock(size: (Int, Int), location: (Int, Int), 
                             borderColor: Color, bgColor: Color, alpha: Int,
                             fontColor: Color, font: Font, 
                             nicknameColor: Color, nicknameFont: Font,
                             messageSize: Int, 
                             backgroundImage: Option[String] = None) extends Notification 
                                               with NotificationTheme 
                                               with NotificationWindow 
                                               with SWTHelper
{
    val display = Display.getDefault
    val shell = new Shell(display, SWT.NO_TRIM|SWT.ON_TOP|SWT.RESIZE)
    val label = createContentLabel()
    var messages: List[IRCMessage] = Nil
    val (inputLabel, inputText) = createChatInputBox()

    def createChatInputBox() = 
    {
        val label = new Label(shell, SWT.LEFT)
        val text = new Text(shell, SWT.BORDER)

        label.setText("聊天：")
        label.setFont(font)
        label.setForeground(fontColor)
        
        val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)

        text.setBackground(bgColor)
        text.setForeground(fontColor)
        text.setFont(font)
        text.setLayoutData(layoutData)
        text.addTraverseListener(new TraverseListener() {
            override def keyTraversed(e: TraverseEvent) {
                if (e.detail == SWT.TRAVERSE_RETURN && text.getText.trim.length > 0) {
                    val message = text.getText.trim()

                    MainWindow.getIRCBot.foreach { bot => 
                        bot.getChannels.foreach { channel =>
                            bot.sendMessage(channel, message)
                        }
                    }

                    NotificationBlock.this.addMessage(ChatMessage(MainWindow.getNickname, false, message))
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

    def addMessage(newMessage: IRCMessage)
    {
        messages = (newMessage :: messages).take(messageSize)
        updateMessages()
    }
    
    def updateMessages()
    {
        display.syncExec (new Runnable {
            override def run () {
                if (!shell.isDisposed) {

                    val regex = """\w+:""".r
                    val message = messages.take(messageSize).
                                  reverse.map(_.toString).mkString("\n")

                    label.setText(message)

                    val styles = regex.findAllIn(message).matchData.map { data => 
                        val style = new StyleRange
                        style.start = data.start
                        style.length = data.end - data.start
                        style.foreground = nicknameColor
                        style.font = nicknameFont
                        style
                    }

                    styles.foreach { style =>
                        label.setStyleRange(style)
                    }
                }
            }
        })
    }

    def this()
    {
        this(
            (300, 448), (100, 100), 
            MyColor.White, MyColor.Black, 210, 
            MyColor.White, MyFont.DefaultFont, 
            MyColor.White, MyFont.DefaultFont,
            10
        )
    }

    def setLayout()
    {
        val layout = new GridLayout(2, false)
        layout.marginLeft = 5
        layout.marginRight = 5
        layout.marginTop = 5
        layout.marginBottom = 5

        shell.setLayout(layout)
        label.setFont(font)
        label.setForeground(fontColor)
        label.setLineSpacing(5)
        label.setEnabled(false)
        label.addModifyListener(new ModifyListener() {
            override def modifyText(e: ModifyEvent) {
                label.setTopIndex(label.getLineCount - 1)
            }
        })

    }

    def setMoveAndResize()
    {
        var isResize = false
        var offsetX = 0
        var offsetY = 0

        shell.addMouseListener(new MouseAdapter() {
            override def mouseDown(e: MouseEvent) {
                offsetX = e.x
                offsetY = e.y

                isResize = 
                    e.x >= (shell.getSize.x - 20) && e.x <= shell.getSize.x &&
                    e.y >= (shell.getSize.y - 20) && e.y <= shell.getSize.y

            }

            override def mouseUp(e: MouseEvent) {
                offsetX = 0
                offsetY = 0
                isResize = false
            }
        })

        shell.addMouseMoveListener(new MouseMoveListener() {

            private var isResizing = false

            def isCorner(e: MouseEvent) = {
                e.x >= (shell.getSize.x - 20) && e.x <= shell.getSize.x &&
                e.y >= (shell.getSize.y - 20) && e.y <= shell.getSize.y
            }

            def setResizeCursor(e: MouseEvent)
            {
                if (isCorner(e) && !isResizing && !display.isDisposed) {
                    shell.setCursor(display.getSystemCursor(SWT.CURSOR_SIZESE))
                    isResizing = true
                } else if (isResizing && !display.isDisposed) {
                    shell.setCursor(display.getSystemCursor(SWT.CURSOR_ARROW))
                    isResizing = false
                }
            }

            def moveWindow(e: MouseEvent)
            {
                val absX = shell.getLocation.x + e.x
                val absY = shell.getLocation.y + e.y
                shell.setLocation(absX - offsetX, absY - offsetY)
                MainWindow.blockSetting.locationX.setText((absX - offsetX).toString)
                MainWindow.blockSetting.locationY.setText((absY - offsetY).toString)
            }

            def resizeWindow(e: MouseEvent)
            {
                shell.setSize(e.x, e.y)
                MainWindow.blockSetting.width.setText(e.x.toString)
                MainWindow.blockSetting.height.setText(e.y.toString)
            }

            override def mouseMove(e: MouseEvent) 
            {
                val isDrag = (e.stateMask & SWT.BUTTON1) != 0
                val shouldMove = isDrag && !isResize
                val shouldResize = isDrag && isResize

                setResizeCursor(e)

                (shouldResize, shouldMove) match {
                    case (true, _) => resizeWindow(e)
                    case (_, true) => moveWindow(e)
                    case (_, _)    => // Do nothing
                }
            }
        })


    }

    def open()
    {
        val optionBGImage = backgroundImage.flatMap { file => 
            try { 
                Some(new Image(display, file))
            } catch {
                case e => None
            }
        }

        optionBGImage match {
            case None => setBackground()
            case Some(null) => setBackground()
            case Some(image) => 
                    shell.setBackgroundImage(image)
                    shell.setBackgroundMode(SWT.INHERIT_DEFAULT)
        }

        setLayout()
        setMoveAndResize()

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

