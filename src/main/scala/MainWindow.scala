package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StackLayout

import org.eclipse.swt._

class Preference extends SWTHelper
{
    import java.util.prefs.Preferences

    val preference = Preferences.userNodeForPackage(classOf[Preference])

    println("abs:" + preference.absolutePath())

    def read(setting: BlockSetting)
    {
        setting.locationX.setText(preference.getInt("BlockX", 100).toString)
        setting.locationY.setText(preference.getInt("BlockY", 100).toString)
        setting.width.setText(preference.getInt("BlockWidth", 200).toString)
        setting.height.setText(preference.getInt("BlockHeight", 400).toString)

        val bgColor = new Color(
            Display.getDefault, 
            preference.getInt("BGRed", 0),
            preference.getInt("BGGreen", 0),
            preference.getInt("BGBlue", 0)
        )

        val fontColor = new Color(
            Display.getDefault,
            preference.getInt("FontRed", 255),
            preference.getInt("FontGreen", 255),
            preference.getInt("FontBlue", 255)
        )

        val borderColor = new Color(
            Display.getDefault,
            preference.getInt("BorderRed", 255),
            preference.getInt("BorderGreen", 255),
            preference.getInt("BorderBlue", 255)
        )

        setting.bgColor = bgColor
        setting.fontColor = fontColor
        setting.borderColor = borderColor
        setting.bgButton.setText(bgColor)
        setting.fgButton.setText(fontColor)
        setting.borderButton.setText(borderColor)

        val font = new Font(
            Display.getDefault, 
            preference.get("FontName", MyFont.DefaultFontName),
            preference.getInt("FontHeight", MyFont.DefaultFontSize),
            preference.getInt("FontStyle", MyFont.DefaultFontStyle)
        )

        setting.messageFont = font
        setting.fontButton.setText(font)

        val transparent = preference.getInt("Transparent", 20)
        setting.transparentScale.setSelection(transparent)
        setting.transparentLabel.setText(
            setting.alphaTitle + transparent + "%"
        )

        setting.messageSizeSpinner.setSelection(
            preference.getInt("MessageSize", 10)
        )
    }

    def save(setting: BlockSetting)
    {
        // 視窗位置、大小
        preference.putInt("BlockX", setting.locationX.getText.toInt)
        preference.putInt("BlockY", setting.locationY.getText.toInt)
        preference.putInt("BlockWidth", setting.width.getText.toInt)
        preference.putInt("BlockHeight", setting.height.getText.toInt)

        // 配色
        preference.putInt("BGRed", setting.bgColor.getRed)
        preference.putInt("BGGreen", setting.bgColor.getGreen)
        preference.putInt("BGBlue", setting.bgColor.getBlue)
        preference.putInt("FontRed", setting.fontColor.getRed)
        preference.putInt("FontGreen", setting.fontColor.getGreen)
        preference.putInt("FontBlue", setting.fontColor.getBlue)
        preference.putInt("BorderRed", setting.borderColor.getRed)
        preference.putInt("BorderGreen", setting.borderColor.getGreen)
        preference.putInt("BorderBlue", setting.borderColor.getBlue)
        preference.putInt("Transparent", setting.transparentScale.getSelection)

        // 字型
        val fontData = setting.messageFont.getFontData()(0)
        preference.put("FontName", fontData.getName)
        preference.putInt("FontHeight", fontData.getHeight)
        preference.putInt("FontStyle", fontData.getStyle)

        preference.putInt(
            "MessageSize", 
            setting.messageSizeSpinner.getSelection
        )
    }
}

object MainWindow extends SWTHelper
{
    Display.setAppName("IRCBalloon")

    val display = new Display
    val shell = new Shell(display)

    val displayStackLayout = new StackLayout

    val logginLabel = createLabel("登入方式：")
    val logginTab = createTabFolder()
    val ircSetting = new IRCSetting(logginTab, e => updateConnectButtonState())
    val justinSetting = new JustinSetting(logginTab, e => updateConnectButtonState())

    val displayLabel = createLabel("顯示方式：")
    val displayTab = createTabFolder()
    val blockSetting = new BlockSetting(displayTab, e => updateConnectButtonState())
    val balloonSetting = new BalloonSetting(displayTab, e => updateConnectButtonState())

    val connectButton = createConnectButton()
    val logTextArea = createLogTextArea()

    private var ircBot: Option[IRCBot] = None
    private var notification: Option[Notification] = None

    def getIRCBot() = ircBot

    def getNickname() = {
        logginTab.getSelectionIndex match {
            case 0 => ircSetting.nickname.getText.trim
            case 1 => justinSetting.username.getText.trim
        }
    }

    def createLabel(title: String)
    {
        val label = new Label(shell, SWT.LEFT)
        label.setText(title)
        label
    }

    def createTabFolder() = 
    {
        val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
        val tabFolder = new TabFolder(shell, SWT.NONE)
        tabFolder.setLayoutData(layoutData)
        tabFolder
    }

    def getAppIcon() =
    {
        new Image(display, getClass().getResourceAsStream("/appIcon.png"));
    }

    def setTrayIcon()
    {
        val tray = display.getSystemTray()

        if (tray != null) {
            val trayIcon = new TrayItem (tray, SWT.NONE)
            trayIcon.setImage(getAppIcon)
            trayIcon.addSelectionListener { e: SelectionEvent =>
                notification.foreach(_.onTrayIconClicked())
            }
        }
    }

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
        val text = new Text(shell, SWT.BORDER|SWT.MULTI|SWT.WRAP|SWT.V_SCROLL|SWT.READ_ONLY)
        layoutData.horizontalSpan = 2
        text.setLayoutData(layoutData)
        text
    }

    def updateConnectButtonState()
    {
        val connectSettingOK = 
            (logginTab.getSelectionIndex == 0 && ircSetting.isSettingOK) ||
            (logginTab.getSelectionIndex == 1 && justinSetting.isSettingOK)

        val displayStettingOK = 
            (displayTab.getSelectionIndex == 0 && blockSetting.isSettingOK) ||
            (displayTab.getSelectionIndex == 1 && blockSetting.isSettingOK)

        connectButton.setEnabled(connectSettingOK && displayStettingOK)
    }

    def createIRCBot(callback: String => Any, onError: Exception => Any) =
    {
        logginTab.getSelectionIndex match {
            case 0 => ircSetting.createIRCBot(callback, appendLog _, onError)
            case 1 => justinSetting.createIRCBot(callback, appendLog _, onError)
        }
    }

    def createNotificationService() = {
        displayTab.getSelectionIndex match {
            case 0 => blockSetting.createNotificationBlock
            case 1 => balloonSetting.createBalloonController
        }
    }

    def setConnectButtonListener()
    {
        def toggleConnectButton()
        {
            connectButton.setSelection(!connectButton.getSelection)
        }

        def onError(exception: Exception) = {
            println("===> From MainWindow.onError")
            exception.printStackTrace()
            displayError(exception, () => { stopBot(); toggleConnectButton()})
        }

        def updateNotification(message: String)
        {
            notification.foreach(_.addMessage(message))
        }

        def startBot()
        {
            setUIEnabled(false)
            logTextArea.setText("開始連線至 IRC 伺服器，請稍候……\n")
            notification = Some(createNotificationService)
            notification.foreach { block =>
                block.open()
                block.addMessage("開始連線至 IRC 伺服器，請稍候……")
                ircBot = Some(createIRCBot(updateNotification _, onError _))
                ircBot.foreach(_.start())
            }
        }

        def stopBot()
        {
            ircBot.foreach(_.stop())
            notification.foreach(_.close)
            ircBot = None
            notification = None
            setUIEnabled(true)
        }

        connectButton.addSelectionListener { e: SelectionEvent =>
            connectButton.getSelection match {
                case true => startBot()
                case false => stopBot()
            }
        }
    }

    def displayError(exception: Exception, callback: () => Any)
    {
        display.syncExec(new Runnable() {
            def outputToLogTextArea()
            {
                logTextArea.append(exception.toString + "\n")
                exception.getStackTrace.foreach { trace =>
                    logTextArea.append("\t at " + trace.toString + "\n")
                }
            }

            override def run() {
                val dialog = new MessageBox(MainWindow.shell, SWT.ICON_ERROR)

                outputToLogTextArea()
                dialog.setMessage("錯誤：" + exception.getMessage)
                dialog.open()
                callback()
                setUIEnabled(true)
            }
        })
    }

    def setUIEnabled(isEnabled: Boolean)
    {
        logginTab.setEnabled(isEnabled)
        displayTab.setEnabled(isEnabled)
        blockSetting.setUIEnabled(isEnabled)
        balloonSetting.setUIEnabled(isEnabled)
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

    def setLayout()
    {
        val gridLayout = new GridLayout(1,  false)
        shell.setLayout(gridLayout)
    }

    def main(args: Array[String])
    {   
        val preference = new Preference

        setLayout()
        setConnectButtonListener()
        setTrayIcon()

        preference.read(blockSetting)

        shell.setText("IRC 聊天通知")
        shell.setImage(getAppIcon)
        shell.pack()
        shell.addShellListener(new ShellAdapter() {
            override def shellClosed(e: ShellEvent) {
                preference.save(blockSetting)
                //preference.preference.clear()
            }
        })
        shell.open()

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose()
        sys.exit()
    }
}
