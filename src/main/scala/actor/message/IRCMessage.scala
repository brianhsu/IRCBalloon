package org.bone.ircballoon.actor.message

import org.bone.ircballoon.model.TwitchUser
import org.bone.ircballoon.model.IRCUser
import org.bone.ircballoon.I18N.i18n._

import java.util.Date
import java.text.SimpleDateFormat

trait HasUser
{
  import org.bone.ircballoon.Preference.displayAvatar
  import org.bone.ircballoon.Preference.onlyAvatar

  val user: IRCUser

  lazy val twitchUser = TwitchUser(user.nickname)

  /**
   *  加入 Avatar 代碼
   *
   *  如果有開啟 Avatar 功能，而且該使用者有 Avatar，就加
   *  入 Avatar 控制碼 [nickname]，UI 看到這個控制碼的時
   *  候會取代為使用者的 Avatar。
   */
  def userDisplay = twitchUser.avatar match {
    case Some(image) if displayAvatar && onlyAvatar => s"[${twitchUser.username}] :"
    case Some(image) if displayAvatar => s"[${twitchUser.username}] ${twitchUser.username}:"
    case _ => s"${twitchUser.username}:"
  }
}

sealed trait IRCMessage {
  val timestamp: Date

  def formattedTimestamp = {
    new SimpleDateFormat("HH:mm").format(timestamp)
  }
}

case class SystemNotice(message: String) extends IRCMessage {

  val timestamp: Date = new Date

  override def toString = message
}

case class Message(message: String, user: IRCUser) extends IRCMessage with HasUser {

  val timestamp: Date = new Date
   
  override def toString = user.isOP || user.isBroadcaster match {
    case true  => s"[OP] ${userDisplay} ${message}"
    case false => s"${userDisplay} ${message}"
  }
}

case class Action(action: String, user: IRCUser) extends IRCMessage with HasUser {
  val timestamp: Date = new Date

  override def toString = tr("[Action] %s %s") format(userDisplay, action)
}

case class Part(reason: String, channel: String, user: IRCUser) extends IRCMessage with HasUser {
  val timestamp: Date = new Date

  override def toString = tr("[SYS] %s has left") format(userDisplay)
}

case class Join(channel: String, user: IRCUser) extends IRCMessage with HasUser {
  val timestamp: Date = new Date

  override def toString = tr("[SYS] %s has joined") format(userDisplay)
}

case class Quit(user: IRCUser, reason: String) extends IRCMessage with HasUser {
  val timestamp: Date = new Date

  override def toString = tr("[SYS] %s has left") format(userDisplay)
}

