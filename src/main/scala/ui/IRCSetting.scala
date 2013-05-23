package org.bone.ircballoon

import org.bone.ircballoon.actor.model.IRCInfo

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StackLayout

import org.eclipse.swt._
import I18N.i18n._

class IRCSetting(parent: TabFolder, onModify: ModifyEvent => Any) extends 
      Composite(parent, SWT.NONE) with SWTHelper
{
  val tabItem = new TabItem(parent, SWT.NONE)
  val gridLayout = new GridLayout(2,  false)
  val hostText = createText(this, tr("IRC Host:"))
  val portText = createText(this, tr("IRC Port:"))
  val password = createText(this, tr("IRC Password:"), SWT.PASSWORD)
  val nickname = createText(this, tr("Nickname:"))
  val channel = createText(this, tr("Channel:"))
  val (onJoinButton, onLeaveButton) = createJoinLeaveButton(this)

  def getPassword = Some(password.getText.trim).filterNot(_.isEmpty)

  def getIRCInfo: IRCInfo = {

    if (!isSettingOK) {
      throw new Exception(tr("IRC Setting is not completed"))
    }

    IRCInfo(
      hostText.getText, 
      portText.getText.toInt, 
      nickname.getText, 
      channel.getText,
      getPassword
    )

  }

  def createIRCBot(callback: IRCMessage => Any, 
                   onLog: String => Any, 
                   onError: Exception => Any) = 
  {

    if (!isSettingOK) {
      throw new Exception(tr("IRC Setting is not completed"))
    }

    new IRCBot(
      hostText.getText, 
      portText.getText.toInt, 
      nickname.getText, 
      getPassword, channel.getText, 
      callback, onLog, onError,
      onJoinButton.getSelection, 
      onLeaveButton.getSelection
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

  def setUIEnabled(isEnabled: Boolean)
  {
    this.hostText.setEnabled(isEnabled)
    this.portText.setEnabled(isEnabled)
    this.password.setEnabled(isEnabled)
    this.nickname.setEnabled(isEnabled)
    this.channel.setEnabled(isEnabled)
  }


  this.setDefaultValue()
  this.setTextVerify()
  this.setModifyListener()
  this.setLayout(gridLayout)
  this.tabItem.setText("IRC")
  this.tabItem.setControl(this)
}

