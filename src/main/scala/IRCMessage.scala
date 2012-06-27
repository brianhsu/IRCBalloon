package org.bone.ircballoon

sealed trait IRCMessage {
    val message: String

    override def toString = this match {
        case ChatMessage(nickname, true, content)   => "[OP] %s: %s" format(nickname, content)
        case ChatMessage(nickname, false, content)  => "%s: %s" format(nickname, content)
        case ActionMessage(nickname, isOp, content) => "[動作] %s %s" format(nickname, content)
        case SystemMessage(content) => content
    }

}

case class ChatMessage(nickname: String, isOp: Boolean, message: String) extends IRCMessage
case class ActionMessage(nickname: String, isOp: Boolean, message: String) extends IRCMessage
case class SystemMessage(message: String) extends IRCMessage

