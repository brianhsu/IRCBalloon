package org.bone.ircballoon

import org.jibble.pircbot.PircBot

object IRCBot
{
    def doNothing(message: String) {}
}

class IRCBot(hostname: String, port: Int, nickname: String, 
             password: Option[String], channel: String, 
             callback: String => Any = IRCBot.doNothing,
             onLog: String => Any = IRCBot.doNothing) extends PircBot
{
    override def onMessage(channel: String, sender: String, login: String, 
                           hostname: String, message: String) 
    {
        callback("%s: %s" format(sender, message))
    }

    override def onJoin(channel: String, sender: String, login: String, hostname: String)
    {
        callback("[系統] %s 加入聊天室" format(sender))
    }

    override def onQuit(sourceNick: String, sourceLogin: String, sourceHostname: String,
                        reason: String)
    {
        callback("[系統] %s 離開聊天室" format(sourceNick))
    }

    private def connect()
    {
        password match {
            case None => super.connect(hostname, port)
            case Some(password) => super.connect(hostname, port, password)
        }
    }

    def startLogging()
    {
        this.setAutoNickChange(true)
        this.setVerbose(true)
        this.setName(nickname)
        this.connect()
        this.joinChannel(channel)
    }
}

