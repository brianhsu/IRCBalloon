package org.bone.ircballoon.actor.message

import org.bone.ircballoon.actor.model.IRCUser


sealed trait IRCMessage

case class SystemNotice(message: String) extends IRCMessage
case class Message(message: String, user: IRCUser) extends IRCMessage
case class Action(action: String, user: IRCUser) extends IRCMessage
case class Part(reason: String, channel: String, user: IRCUser) extends IRCMessage
case class Join(channel: String, user: IRCUser) extends IRCMessage
case class Quit(user: IRCUser, reason: String) extends IRCMessage

