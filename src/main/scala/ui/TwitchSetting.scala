package org.bone.ircballoon

import org.bone.ircballoon.model.IRCInfo

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StackLayout
import org.eclipse.swt.browser._

import org.eclipse.swt._

import java.net.URL
import scala.io.Source
import scala.util.parsing.json.JSON

import I18N.i18n._

class TwitchAuthDialog(parent: Shell) extends Dialog(parent, SWT.APPLICATION_MODAL)
                                      with SWTHelper
{
  var result: Option[(String, String)] = None // (username, accessToken)
  val display = parent.getDisplay()
  val shell = new Shell(parent, SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL)
  val browser = new Browser(shell, SWT.NONE)

  def displayError(message: String) {
    val messageBox = new MessageBox(shell, SWT.ERROR|SWT.OK)
    messageBox.setMessage(message)
    messageBox.open()
  }

  def open() =
  {
    val layout = new FillLayout
    shell.setLayout(layout)
    shell.setText(tr("Twitch Authorization"))
    shell.pack()
    shell.setSize(640, 480)
    shell.open()

    browser.setUrl("https://api.twitch.tv/kraken/oauth2/authorize?response_type=token&client_id=jcb3b04wnxtizw0syccalv3s801yzlz&redirect_uri=http://localhost/auth&scope=chat_login&user_read")
    browser.addLocationListener(new LocationListener() {
      override def changed(event: LocationEvent) {        
      }

      override def changing(event: LocationEvent) {
        val url = event.location
        try {
          if (url.startsWith("http://localhost/auth")) {
            val accessToken = url.drop(22).split("&").map(_.split("=")).filter(x => x(0) == "access_token").flatten
            val token = accessToken(1).trim
            val httpURLConnection = new URL("https://api.twitch.tv/kraken/user").openConnection
            httpURLConnection.addRequestProperty("Accept", "application/vnd.twitchtv.v2+json")
            httpURLConnection.addRequestProperty("Authorization", "OAuth "+token)
            httpURLConnection.addRequestProperty("Client-ID", "jcb3b04wnxtizw0syccalv3s801yzlz&redirect_uri")
            httpURLConnection.connect()
            val userInfoJSON = Source.fromInputStream(httpURLConnection.getInputStream)("UTF-8").mkString
            val userInfoHolder = JSON.parseFull(userInfoJSON)

            for (userInfo <- userInfoHolder) {
              userInfo match {
                case x: Map[_, _] => 
                  val name = x.asInstanceOf[Map[String,Any]]("name").toString
                  result = Some((name, token))
                case _ =>
              }
            }

            shell.dispose()
          }
        } catch {
          case e: Exception => displayError(tr("Cannot get IRC OAuth access token"))
        }
      }
    })

    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) display.sleep()
    }

    result

  }

}

class TwitchSetting(parent: TabFolder, onModify: ModifyEvent => Any) extends 
       Composite(parent, SWT.NONE) with SWTHelper
{
  val tabItem = new TabItem(parent, SWT.NONE)
  val gridLayout = new GridLayout(3,  false)
  val username = createText(this, tr("Username:"))
  val emptyPlaceHolder = new Label(this, SWT.NONE)
  val password = createText(this, tr("Password:"), SWT.PASSWORD)
  password.setEnabled(false)
  username.setEnabled(false)
  val button = new Button(this, SWT.PUSH|SWT.BORDER)
  button.setText(tr("Login Twitch"))
  button.addSelectionListener { e: SelectionEvent =>
    val dialog = new TwitchAuthDialog(getShell)
    val accessTokenHolder = dialog.open()
    accessTokenHolder.foreach { case(twitterUsername, token) => 
      username.setText(twitterUsername)
      password.setText(token)
    }
  }
  val (onJoinButton, onLeaveButton) = createJoinLeaveButton(this)


  def getIRCInfo: IRCInfo = {
    val hostname = "irc.twitch.tv"
    val password = Some("oauth:"+this.password.getText.trim)
    val channel = "#%s" format(username.getText)

    IRCInfo(
      hostname, 6667, username.getText, channel, password, 
      onJoinButton.getSelection, onLeaveButton.getSelection
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

  def setUIEnabled(isEnabled: Boolean)
  {
    this.username.setEnabled(isEnabled)
    this.password.setEnabled(isEnabled)
  }


  this.setLayout(gridLayout)
  this.setModifyListener()
  this.tabItem.setText("Twitch")
  this.tabItem.setControl(this)
}

