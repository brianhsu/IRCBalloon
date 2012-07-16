package org.bone.ircballoon

import I18N.i18n._

sealed trait IRCMessage

trait HasUser
{
    val nickname: String

    import Avatar.displayAvatar
    import Avatar.onlyAvatar

    /**
     *  視狀況取代使用者名稱取代為 Twitch 的暱稱
     *
     */
    def convertedNickname = Avatar.usingTwitchNickname match {
        case true => Avatar.getTwitchNicknameCache(nickname).getOrElse(nickname)
        case false => nickname
    }

    /**
     *  加入 Avatar 代碼
     *
     *  如果有開啟 Avatar 功能，而且該使用者有 Avatar，就加
     *  入 Avatar 控制碼 [nickname]，UI 看到這個控制碼的時
     *  候會取代為使用者的 Avatar。
     */
    def userDisplay = Avatar(nickname) match {
        case Some(image) if displayAvatar && onlyAvatar => "[%s]" format(nickname)
        case Some(image) if displayAvatar => "[%s] %s" format(nickname, convertedNickname)
        case _ => convertedNickname
    }
}

/**
 *  IRC 聊天訊息
 *
 *  @param  nickname    使用者暱稱
 *  @param  isOp        使用者是否為 IRC 頻道管理員
 *  @param  message     訊息
 */
case class ChatMessage(nickname: String, isOp: Boolean, 
                       message: String) extends IRCMessage with HasUser
{
    override def toString = isOp match {
        case true => "[OP] %s: %s" format(userDisplay, message)
        case false => "%s: %s" format(userDisplay, message)
    }
}

/**
 *  IRC 動作訊息
 *
 *  @param  nickname    使用者暱稱
 *  @param  isOp        使用者是否為 IRC 頻道管理員
 *  @param  message     使用者的動作
 */
case class ActionMessage(nickname: String, isOp: Boolean, 
                         message: String) extends IRCMessage with HasUser
{
    override def toString = tr("[Action] %s %s") format(userDisplay, message)
}

/**
 *  IRC 系統訊息
 *
 *  @param  message     系統訊息
 */
case class SystemMessage(message: String) extends IRCMessage
{
    override def toString = message
}

