package org.bone.ircballoon

import org.jibble.pircbot.PircBot

object IRCBot
{
    def doNothing(message: String) {}
    def doNothing(message: IRCMessage) {}
    def doNothing(exception: Exception) {
        println("From IRCBot.doNothing:")
        exception.printStackTrace()
    }
}

class IRCBot(hostname: String, port: Int, nickname: String, 
             password: Option[String], channel: String, 
             callback: IRCMessage => Any = IRCBot.doNothing,
             onLog: String => Any = IRCBot.doNothing,
             onError: Exception => Any = IRCBot.doNothing,
             showJoin: Boolean = false,
             showLeave: Boolean = false) extends PircBot
{
    private var opUser: Set[String] = Set()

    override def onDeop(channel: String, sourceNick: String, sourceLogin: String,
                        sourceHostname: String, recipient: String)
    {
        opUser -= recipient
        println("onDeop:" + opUser)
    }
   
    override def onOp(channel: String, sourceNick: String, sourceLogin: String,
                      sourceHostname: String, recipient: String)
    {
        opUser += recipient
        println("onOp:" + opUser)
    }

    override def onAction(sender: String, login: String, hostname: String, target: String,
                          action: String)
    {
        callback(ActionMessage(sender, opUser.contains(sender), action))
    }

    override def onMessage(channel: String, sender: String, login: String, 
                           hostname: String, message: String) 
    {
        callback(ChatMessage(sender, opUser.contains(sender), message))
    }

    override def onPart(channel: String, sender: String, login: String, hostname: String)
    {
        if (showLeave) {
            callback(SystemMessage("[系統] %s 離開聊天室" format(sender)))
        }
    }

    override def onJoin(channel: String, sender: String, login: String, hostname: String)
    {
        showJoin match {
            case true  => callback(SystemMessage("[系統] %s 加入聊天室" format(sender)))
            case false if (sender == nickname) => 
                callback(SystemMessage("[系統] %s 加入聊天室" format(sender)))
            case _ => 
        }
    }

    override def onQuit(sourceNick: String, sourceLogin: String, sourceHostname: String,
                        reason: String)
    {
        if (showLeave) {
            callback(SystemMessage("[系統] %s 離開聊天室" format(sourceNick)))
        }
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
                    IRCBot.this.setEncoding("UTF-8")
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

