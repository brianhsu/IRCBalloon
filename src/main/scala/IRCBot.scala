package org.bone.ircballoon

import org.jibble.pircbot.PircBot

class IRCBot(hostname: String, port: Int, nickname: String, 
             password: Option[String], channel: String) extends PircBot
{
    override def onMessage(channel: String, sender: String, login: String, 
                           hostname: String, message: String) 
    {
        println("[%s] %s(%s @ %s): %s" format(channel, sender, login, hostname, message))
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
        this.setVerbose(true)
        this.setName(nickname)
        this.connect()
        this.joinChannel(channel)
    }
}

