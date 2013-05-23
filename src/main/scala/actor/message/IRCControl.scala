package org.bone.ircballoon.actor.message

import org.bone.ircballoon.actor.model.IRCInfo

sealed trait IRCControl

case class StartIRCBot(info: IRCInfo) extends IRCControl
case class SendIRCMessage(message: String) extends IRCControl
case object StopIRCBot extends IRCControl

