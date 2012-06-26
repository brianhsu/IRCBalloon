package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText

import org.eclipse.swt._
import scala.math._

trait NotificationWindow
{
    def display: Display
    def shell: Shell
    def bgColor: Color
    def borderColor: Color
}

trait Notification
{
    def addMessage(newMessage: IRCMessage): Unit
    def open(): Unit
    def close(): Unit
    def onTrayIconClicked(): Unit = {
    }
}
