package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StackLayout

import org.eclipse.swt._

object Main extends SWTHelper
{
    val display = new Display
    val shell = new Shell(display)
    var notification: NotificationBlock = null

    val stackLayout = new StackLayout
    val displayStackLayout = new StackLayout

    val logginType = createLogginType()
    val settingGroup = createSettingGroup()
    val ircButton = createIRCButton()
    val justinButton = createJustinButton()
    val settingPages = createSettingPages()
    val ircSetting = new IRCSetting(settingPages, e => updateConnectButtonState())
    val justinSetting = new JustinSetting(settingPages, e => updateConnectButtonState())

    val displayType = createDisplayType()
    val displayGroup = createDisplayGroup()
    val blockButton = createBlockButton()
    val balloonButton = createBalloonButton()

    val displayPages = createDisplayPages()
    val blockSetting = new BlockSetting(displayPages, e => updateConnectButtonState())
    val balloonSetting = new BalloonSetting(displayPages)

    val connectButton = createConnectButton()
    val logTextArea = createLogTextArea()

    def appendLog(message: String)
    {
        display.asyncExec(new Runnable() {
            override def run()
            {
                logTextArea.append(message + "\n")
            }
        })
    }

    def createLogTextArea() =
    {
        val layoutData = new GridData(SWT.FILL, SWT.FILL, true, true)
        val text = new Text(shell, SWT.MULTI|SWT.WRAP|SWT.V_SCROLL|SWT.READ_ONLY)
        layoutData.horizontalSpan = 2
        text.setLayoutData(layoutData)
        text
    }

    def updateConnectButtonState()
    {
        val connectSettingOK = 
            (ircButton.getSelection == true && ircSetting.isSettingOK) ||
            (justinButton.getSelection == true && justinSetting.isSettingOK)

        val displayStettingOK = 
            (blockButton.getSelection == true && blockSetting.isSettingOK) &&
            (balloonButton.getSelection == false)

        connectButton.setEnabled(connectSettingOK && displayStettingOK)
    }

    def createIRCBot(callback: String => Any) =
    {
        (ircButton.getSelection, justinButton.getSelection) match {
            case (true, false) => ircSetting.createIRCBot(callback, appendLog _)
            case (false, true) => justinSetting.createIRCBot(callback, appendLog _)
        }
    }

    def setConnectButtonListener()
    {
        var ircBot: Option[IRCBot] = None
        var notification: Option[NotificationBlock] = None

        def startBot()
        {
            def updateNotification(message: String)
            {
                notification.foreach(_.addMessage(message))
            }

            logTextArea.setText("開始連線至 IRC 伺服器，請稍候……\n")
            notification = Some(blockSetting.createNotificationBlock)
            notification.foreach { block =>
                block.open()
                block.addMessage("開始連線至 IRC 伺服器，請稍候……")
                ircBot = Some(createIRCBot(updateNotification _))
                ircBot.foreach(_.startLogging())
            }
        }

        def stopBot()
        {
            ircBot.foreach{ bot => if(bot.isConnected) bot.dispose }
            notification.foreach(_.close)
            ircBot = None
            notification = None
        }

        connectButton.addSelectionListener { e: SelectionEvent =>
            try {
                connectButton.getSelection match {
                    case true => startBot()
                    case false => stopBot()
                }
            } catch {
                case e: Exception => 
                    connectButton.setSelection(!connectButton.getSelection)
                    displayError(e, stopBot _)
            }
        }
    }

    def displayError(exception: Exception, callback: () => Any)
    {
        display.syncExec(new Runnable() {
            override def run() {
                val dialog = new MessageBox(Main.shell, SWT.ICON_ERROR)
                dialog.setMessage("錯誤：" + exception.getMessage)
                dialog.open()
                callback()
            }
        })
    }

    def createConnectButton() =
    {
        val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
        val button = new Button(shell, SWT.TOGGLE)

        layoutData.horizontalSpan = 2
        button.setLayoutData(layoutData)
        button.setText("連線")
        button.setEnabled(false)
        button
    }

    def switchDisplayPages()
    {
        (blockButton.getSelection, balloonButton.getSelection) match {
            case (true, _) => displayStackLayout.topControl = blockSetting
            case (_, true) => displayStackLayout.topControl = balloonSetting
        }

        displayPages.layout()
        updateConnectButtonState()
    }

    def createDisplayPages() =
    {
        val composite = new Composite(shell, SWT.NONE)
        val spanLayout = new GridData(SWT.FILL, SWT.NONE, true, false)
        spanLayout.horizontalSpan = 2
        composite.setLayoutData(spanLayout)
        composite.setLayout(displayStackLayout)
        composite
    }

    def createSettingGroup() =
    {
        val group = new Group(shell, SWT.SHADOW_NONE)
        val spanLayout = new GridData(SWT.FILL, SWT.NONE, true, false)
        group.setLayoutData(spanLayout)
        group.setLayout(new RowLayout)
        group
    }


    def createDisplayGroup() =
    {
        val group = new Group(shell, SWT.SHADOW_NONE)
        val spanLayout = new GridData(SWT.FILL, SWT.NONE, true, false)
        group.setLayoutData(spanLayout)
        group.setLayout(new RowLayout)
        group
    }

    def createDisplayType() = 
    {
        val label = new Label(shell, SWT.LEFT)
        label.setText("顯示方式：")
        label
    }

    def createBlockButton() = 
    {
        val button = new Button(displayGroup, SWT.RADIO)
        button.setText("固定區塊")
        button.setSelection(true)
        button.addSelectionListener { e:SelectionEvent =>
            switchDisplayPages()
        }
        button
    }

    def createBalloonButton() = 
    {
        val button = new Button(displayGroup, SWT.RADIO)
        button.setText("泡泡通知")
        button.addSelectionListener { e: SelectionEvent =>
            switchDisplayPages()
        }
        button
    }

    def switchSettingPages()
    {
        (ircButton.getSelection, justinButton.getSelection) match {
            case (true, _) => stackLayout.topControl = ircSetting
            case (_, true) => stackLayout.topControl = justinSetting
        }

        settingPages.layout()
        updateConnectButtonState()
    }

    def createSettingPages() = 
    {
        val composite = new Composite(shell, SWT.NONE)
        val spanLayout = new GridData(SWT.FILL, SWT.NONE, true, false)
        spanLayout.horizontalSpan = 2
        composite.setLayoutData(spanLayout)
        composite.setLayout(stackLayout)
        composite
    }

    def createLogginType() = 
    {
        val label = new Label(shell, SWT.LEFT|SWT.BORDER)
        label.setText("設定方式：")
        label
    }

    def createIRCButton() =
    {
        val button = new Button(settingGroup, SWT.RADIO)
        button.setText("IRC")
        button.setSelection(true)
        button.addSelectionListener{ e: SelectionEvent =>
            switchSettingPages()
        }
        button
    }
    
    def createJustinButton() = 
    {
        val button = new Button(settingGroup, SWT.RADIO)
        button.setText("Justin / Twitch")
        button.addSelectionListener { e: SelectionEvent =>
            switchSettingPages()
        }
        button
    }

    def setLayout()
    {
        val gridLayout = new GridLayout(2,  false)
        shell.setLayout(gridLayout)
    }

    def main(args: Array[String])
    {
        setLayout()
        switchSettingPages()
        switchDisplayPages()
        setConnectButtonListener()

        shell.setText("IRC 聊天通知")
        shell.pack()
        shell.open()

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose()
        sys.exit()
    }
}
