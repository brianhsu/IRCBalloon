package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StackLayout

import org.eclipse.swt._

class JustinSetting(parent: Composite, onModify: ModifyEvent => Any) extends 
       Composite(parent, SWT.NONE) with SWTHelper
{
    val gridLayout = new GridLayout(2,  false)
    val username = createText(this, "帳號：")
    val password = createText(this, "密碼：", SWT.PASSWORD)

    def createIRCBot(callback: String => Any) =
    {
        val hostname = "%s.jtvirc.com" format(username.getText)
        val password = Some(this.password.getText.trim)
        val channel = "#%s" format(username.getText)
        new IRCBot(
            hostname, 6667, username.getText, 
            password, channel, callback
        )
    }

    def isSettingOK = {
        val username = this.username.getText.trim
        val password = this.password.getText.trim

        username.length > 0 && password.length > 0
    }

    def setModifyListener()
    {
        username.addModifyListener(onModify)
        password.addModifyListener(onModify)
    }

    this.setLayout(gridLayout)
    this.setModifyListener()
}

class IRCSetting(parent: Composite, onModify: ModifyEvent => Any) extends 
      Composite(parent, SWT.NONE) with SWTHelper
{
    val gridLayout = new GridLayout(2,  false)
    val hostText = createText(this, "IRC 伺服器主機：")
    val portText = createText(this, "IRC 伺服器Port：")
    val password = createText(this, "IRC 伺服器密碼：", SWT.PASSWORD)
    val nickname = createText(this, "暱稱：")
    val channel = createText(this, "聊天頻道：")

    def getPassword = password.getText.trim match {
        case ""    => None
        case value => Some(value)
    }

    def createIRCBot(callback: String => Any) = {

        if (!isSettingOK) {
            throw new Exception("IRC 設定不完整")
        }

        new IRCBot(
            hostText.getText, portText.getText.toInt, nickname.getText, 
            getPassword, channel.getText, callback
        )
    }

    def isSettingOK = {
        val hostname = this.hostText.getText.trim
        val port = this.portText.getText.trim
        val nickname = this.nickname.getText.trim
        val channel = this.channel.getText.trim

        hostname.length > 0 &&
        port.length > 0 &&
        nickname.length > 0 &&
        channel.length > 0 && channel.startsWith("#")
    }

    def setDefaultValue()
    {
        portText.setText("6667")
    }

    def setTextVerify()
    {
        portText.addVerifyListener { e: VerifyEvent => e.doit = e.text.forall(_.isDigit) }
    }

    def setModifyListener()
    {
        hostText.addModifyListener(onModify)
        portText.addModifyListener(onModify)
        password.addModifyListener(onModify)
        nickname.addModifyListener(onModify)
        channel.addModifyListener(onModify)
    }

    this.setDefaultValue()
    this.setTextVerify()
    this.setModifyListener()
    this.setLayout(gridLayout)
}

class BlockSetting(parent: Composite, onModify: ModifyEvent => Any) extends 
      Composite(parent, SWT.NONE) with SWTHelper
{
    var bgColor: Color = MyColor.Black
    var fgColor: Color = MyColor.White
    var messageFont: Font = Display.getDefault.getSystemFont

    val gridLayout = new GridLayout(4, false)
    val locationX = createText(this, "視窗位址 X：")
    val locationY = createText(this, "視窗位址 Y：")
    val width = createText(this, "視窗寬度：")
    val height = createText(this, "視窗高度：")
    val (bgLabel, bgButton) = createColorChooser(this, "背景顏色：", bgColor, bgColor = _)
    val (fgLabel, fgButton) = createColorChooser(this, "文字顏色：", fgColor, fgColor = _)
    val (fontLabel, fontButton) = createFontChooser(this, "訊息字型：", messageFont = _)
    val (transparentLabel, transparentScale) = createScaleChooser(this, "透明度：")
    val (messageSizeLabel, messageSizeSpinner) = createSpinner(this, "訊息數量：", 1, 50)
    val previewButton = createPreviewButton()

    class TestThread(notificationBlock: NotificationBlock) extends Thread
    {
        private var shouldStop = false

        def setStop(shouldStop: Boolean)
        {
            this.shouldStop = shouldStop
        }
        
        override def run ()
        {
            var count = 1

            while (!shouldStop) {
                val message = MessageSample.random(1).head
                notificationBlock.addMessage("[%d] %s" format(count, message))
                count = (count + 1)
                Thread.sleep(1000)
            }
        }
    }

    def createNotificationBlock() = 
    {
        val size = (width.getText.toInt, height.getText.toInt)
        val location = (locationX.getText.toInt, locationY.getText.toInt)
        val messageSize = messageSizeSpinner.getSelection
        val alpha = 255 - (255 * (transparentScale.getSelection / 100.0)).toInt

        NotificationBlock(
            size, location, 
            MyColor.White, bgColor, alpha, 
            fgColor, messageFont, messageSize
        )
    }

    def setDefaultValue()
    {
        locationX.setText("100")
        locationY.setText("100")
        width.setText("300")
        height.setText("500")
        messageSizeSpinner.setSelection(10)
    }

    def setTextVerify()
    {
        locationX.addVerifyListener { e: VerifyEvent => e.doit = e.text.forall(_.isDigit) }
        locationY.addVerifyListener { e: VerifyEvent => e.doit = e.text.forall(_.isDigit) }
        width.addVerifyListener { e: VerifyEvent => e.doit = e.text.forall(_.isDigit) }
        height.addVerifyListener { e: VerifyEvent => e.doit = e.text.forall(_.isDigit) }
    }

    def createPreviewButton() =
    {
        var notificationBlock: Option[NotificationBlock] = None
        var testThread: Option[TestThread] = None

        val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
        val button = new Button(this, SWT.PUSH)

        def startPreview ()
        {
            notificationBlock = Some(createNotificationBlock)
            notificationBlock.foreach{ block => 
                block.open()
                testThread = Some(new TestThread(block))
                testThread.foreach(_.start)
            }
            button.setText("停止預覽")
        }

        def stopPreview()
        {
            button.setText("開始預覽")
            notificationBlock.foreach{ block =>
                testThread.foreach{_.setStop(true)}
                testThread = None
                block.close()
            }
            notificationBlock = None
        }

        layoutData.horizontalSpan = 2
        button.setLayoutData(layoutData)
        button.setText("開始預覽")
        button.addSelectionListener { e: SelectionEvent =>
            notificationBlock match {
                case None    => startPreview()
                case Some(x) => stopPreview()
            }
        }
        button
    }

    def isSettingOK = {
        locationX.getText.trim.length > 0 &&
        locationY.getText.trim.length > 0 &&
        width.getText.trim.length > 0 &&
        height.getText.trim.length > 0
    }

    def setModifyListener() {
        locationX.addModifyListener(onModify)
        locationY.addModifyListener(onModify)
        width.addModifyListener(onModify)
        height.addModifyListener(onModify)
    }

    this.setLayout(gridLayout)
    this.setDefaultValue()
    this.setTextVerify()
    this.setModifyListener()
}

class BalloonSetting(parent: Composite) extends Composite(parent, SWT.NONE) with SWTHelper
{
    val gridLayout = new GridLayout(4, false)
    val locationX = createText(this, "視窗位址 X：")
    val locationY = createText(this, "視窗位址 Y：")

    this.setLayout(gridLayout)

}


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
            case (true, false) => ircSetting.createIRCBot(callback)
            case (false, true) => justinSetting.createIRCBot(callback)
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

            notification = Some(blockSetting.createNotificationBlock)
            notification.foreach { block =>
                block.open()
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

        def updateTitle() {
            connectButton.getSelection match {
                case true  => connectButton.setText("連線")
                case false => connectButton.setText("中斷")
            }
        }

        connectButton.addSelectionListener { e: SelectionEvent =>
            try {
                connectButton.getSelection match {
                    case true => startBot()
                    case false => stopBot()
                }
                updateTitle()
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
