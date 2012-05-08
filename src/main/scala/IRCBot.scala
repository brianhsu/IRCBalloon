package org.bone.ircballoon

import org.jibble.pircbot.PircBot

object IRCBot
{
    def doNothing(message: String) {}
    def doNothing(exception: Exception) {
        println("From IRCBot.doNothing:")
        exception.printStackTrace()
    }
}

class IRCBot(hostname: String, port: Int, nickname: String, 
             password: Option[String], channel: String, 
             callback: String => Any = IRCBot.doNothing,
             onLog: String => Any = IRCBot.doNothing,
             onError: Exception => Any = IRCBot.doNothing) extends PircBot
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

    override def log(line: String)
    {
        super.log(line)
        onLog(line)
    }

    private def connect()
    {
        password match {
            case None => super.connect(hostname, port)
            case Some(password) => super.connect(hostname, port, password)
        }
    }
    
    def stop()
    {
        val thread = new Thread() {
            override def run() {
                if (IRCBot.this.isConnected) {
                    IRCBot.this.disconnect()
                    IRCBot.this.dispose()
                }
            }
        }

        thread.start()
    }

    def start()
    {
        val thread = new Thread() {
            override def run() {
                try {
                    IRCBot.this.setAutoNickChange(true)
                    IRCBot.this.setVerbose(true)
                    IRCBot.this.setName(nickname)
                    IRCBot.this.connect()
                    IRCBot.this.joinChannel(channel)
                } catch {
                    case e: Exception => onError(e)
                }
            }
        }

        thread.start()
    }
}

