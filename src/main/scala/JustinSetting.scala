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

    def createIRCBot(callback: String => Any, onLog: String => Any, onError: Exception => Any) =
    {
        val hostname = "%s.jtvirc.com" format(username.getText)
        val password = Some(this.password.getText.trim)
        val channel = "#%s" format(username.getText)
        new IRCBot(
            hostname, 6667, username.getText, 
            password, channel, callback, onLog, onError
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
}

