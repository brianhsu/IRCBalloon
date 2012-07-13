package org.bone.ircballoon

import I18N.i18n._

sealed trait IRCMessage {
    val message: String
}

trait HasUser
{
    val nickname: String

    import Avatar.displayAvatar
    import Avatar.onlyAvatar

    def userDisplay = Avatar(nickname) match {
        case Some(image) if displayAvatar && onlyAvatar => "[%s]" format(nickname)
        case Some(image) if displayAvatar => "[%s] %s" format(nickname, nickname)
        case _ => nickname
    }
}

case class ChatMessage(nickname: String, isOp: Boolean, 
                       message: String) extends IRCMessage with HasUser
{
    override def toString = isOp match {
        case true => "[OP] %s: %s" format(userDisplay, message)
        case false => "%s: %s" format(userDisplay, message)
    }
}

case class ActionMessage(nickname: String, isOp: Boolean, 
                         message: String) extends IRCMessage with HasUser
{
    override def toString = tr("[Action] %s %s") format(userDisplay, message)
}

case class SystemMessage(message: String) extends IRCMessage
{
    override def toString = message
}

