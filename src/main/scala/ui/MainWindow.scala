package org.bone.ircballoon

import org.bone.ircballoon.actor._
import org.bone.ircballoon.actor.message._

import akka.actor._

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StackLayout
import org.eclipse.swt.custom.ScrolledComposite

import org.eclipse.swt._
import I18N.i18n._

/**
 *  主視窗
 */
object MainWindow extends SWTHelper
{
  Display.setAppName("IRCBalloon")

  val display = new Display
  val shell = new Shell(display)

  val menu = createMenu()
  val logginLabel = createLabel(tr("Login Method"))
  val logginTab = createTabFolder()
  val ircSetting = new IRCSetting(logginTab, e => updateConnectButtonState())
  val justinSetting = new JustinSetting(logginTab, e => updateConnectButtonState())

  val displayLabel = createLabel(tr("Display Method"))
  val displayTab = createTabFolder(true)

  val blockScroll = new ScrolledComposite(displayTab, SWT.V_SCROLL)
  val ballonScroll = new ScrolledComposite(displayTab, SWT.V_SCROLL)

  val blockSetting = new BlockSetting(displayTab, blockScroll, e => updateConnectButtonState())
  val balloonSetting = new BalloonSetting(displayTab, ballonScroll, e => updateConnectButtonState())
  val connectButton = createConnectButton()
  val logTextArea = createLogTextArea()

  lazy val actorSystem = ActorSystem("IRCBalloon")
  lazy val controller = actorSystem.actorOf(Props[ControllerActor])

  def createMenu() =
  {
    val menuBar = new Menu(shell, SWT.BAR)
    val optionHeader = new MenuItem(menuBar, SWT.CASCADE)
    val optionMenu = new Menu(shell, SWT.DROP_DOWN)
    val emoteItem = new MenuItem(optionMenu, SWT.PUSH)
    val avatarItem = new MenuItem(optionMenu, SWT.PUSH)

    optionHeader.setMenu(optionMenu)
    optionHeader.setText(tr("&Preference"))
    emoteItem.setText(tr("Emotes"))
    emoteItem.addSelectionListener { e: SelectionEvent =>
      val emotePreference = new EmoteWindow(shell)
      emotePreference.open()
    }

    avatarItem.setText(tr("Avatar / Nickname"))
    avatarItem.addSelectionListener { e: SelectionEvent =>
      val avatarPreference = new AvatarWindow(shell)
      avatarPreference.open()
    }

    shell.setMenuBar(menuBar)
    menuBar
  }

  def createLabel(title: String) =
  {
    val label = new Label(shell, SWT.LEFT)
    label.setText(title)
    label
  }

  def createTabFolder(adjustHeight: Boolean = false) = 
  {
    val layoutData = new GridData(SWT.FILL, SWT.FILL, true, adjustHeight)
    val tabFolder = new TabFolder(shell, SWT.NONE)

    if (adjustHeight) {
      layoutData.minimumHeight = 250
    }

    tabFolder.setLayoutData(layoutData)
    tabFolder
  }

  def setTrayIcon()
  {
    val tray = display.getSystemTray()

    if (tray != null) {
      val trayIcon = new TrayItem (tray, SWT.NONE)
      trayIcon.setImage(MyIcon.appIcon)
      trayIcon.addSelectionListener { e: SelectionEvent =>
        controller ! ToggleNotification
      }
    }
  }

  def appendLog(message: String)
  {
    if (display.isDisposed) {
      return
    }

    display.asyncExec(new Runnable() {
      override def run()
      {
        if (!logTextArea.isDisposed) {
          logTextArea.append(message + "\n")
        }
      }
    })
  }

  def createLogTextArea() =
  {
    val layoutData = new GridData(SWT.FILL, SWT.FILL, true, true)
    val text = new Text(shell, SWT.BORDER|SWT.MULTI|SWT.WRAP|SWT.V_SCROLL|SWT.READ_ONLY)
    layoutData.horizontalSpan = 2
    layoutData.minimumHeight = 50

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

  def getIRCInfo = logginTab.getSelectionIndex match {
    case 0 => ircSetting.getIRCInfo
    case 1 => justinSetting.getIRCInfo
  }

  def createNotificationService() = {
    displayTab.getSelectionIndex match {
      case 0 => blockSetting.createNotificationBlock
      case 1 => balloonSetting.createBalloonController
    }
  }

  def setConnectButtonListener()
  {
    def startBot()
    {
      val connectMessage = tr("Connecting to IRC server, please wait...\n")

      setUIEnabled(false)
      logTextArea.setText(connectMessage)
      controller ! SetNotification(createNotificationService)
      controller ! StartIRCBot(getIRCInfo)
      controller ! SystemNotice(connectMessage)
    }
  
    def stopBot()
    {
      controller ! StopNotification
      controller ! StopIRCBot
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
        dialog.setMessage(tr("Error:") + exception.getMessage)
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
    ircSetting.setUIEnabled(isEnabled)
    justinSetting.setUIEnabled(isEnabled)
    blockSetting.setUIEnabled(isEnabled)
    balloonSetting.setUIEnabled(isEnabled)
  }

  def createConnectButton() =
  {
    val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
    val button = new Button(shell, SWT.TOGGLE)

    layoutData.horizontalSpan = 2
    button.setLayoutData(layoutData)
    button.setText(tr("Connect"))
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
    setLayout()
    setConnectButtonListener()
    setTrayIcon()

    Preference.read(ircSetting)
    Preference.read(justinSetting)
    Preference.read(blockSetting)
    Preference.read(balloonSetting)
    Preference.readEmotes()
    Preference.readAvatars()

    shell.setText(tr("IRC Notification"))
    shell.setImage(MyIcon.appIcon)

    shell.pack()
    shell.addShellListener(new ShellAdapter() {
      override def shellClosed(e: ShellEvent) {
        Preference.save(ircSetting)
        Preference.save(justinSetting)
        Preference.save(blockSetting)
        Preference.save(balloonSetting)
        Preference.saveEmotes()
        Preference.saveAvatars()
      }
    })
        
    shell.open()
    logginTab.setFocus()

    while (!shell.isDisposed()) {
      if (!display.readAndDispatch ()) display.sleep ();
    }

    display.dispose()
    sys.exit()
  }
}

