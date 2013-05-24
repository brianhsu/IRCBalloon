package org.bone.ircballoon.actor.message

import org.bone.ircballoon.model.IRCInfo

sealed trait IRCControl

case class StartIRCBot(info: IRCInfo) extends IRCControl
case class SendIRCMessage(message: String) extends IRCControl
case object StopIRCBot extends IRCControl
case class IRCLog(line: String) extends IRCControl
case class IRCException(exception: Exception) extends IRCControl
case object CheckIRCAlive extends IRCControl
