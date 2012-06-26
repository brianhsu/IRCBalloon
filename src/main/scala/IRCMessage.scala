package org.bone.ircballoon

sealed trait IRCMessage {
    val message: String
}

case class ChatMessage(nickname: String, isOp: Boolean, message: String) extends IRCMessage
case class ActionMessage(nickname: String, isOp: Boolean, message: String) extends IRCMessage
case class SystemMessage(message: String) extends IRCMessage

