package org.bone.ircballoon

import org.jibble.pircbot.PircBot
import org.pircbotx.PircBotX

import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events._

class IRCBot(hostname: String, port: Int, nickname: String, 
             password: Option[String], channel: String, 
             callback: IRCMessage => Any = IRCBot.doNothing,
             onLog: String => Any = IRCBot.doNothing,
             onError: Exception => Any = IRCBot.doNothing,
             showJoin: Boolean = false,
             showLeave: Boolean = false) extends PircBotX
{

    object Callbacks extends ListenerAdapter[IRCBot]
    {
        private var opUser: Set[String] = Set()

        override def onMessage(event: MessageEvent[IRCBot])
        {
            val nickname = event.getUser.getNick
            callback(ChatMessage(nickname, opUser.contains(nickname), event.getMessage))
        }

        override def onAction(event: ActionEvent[IRCBot])
        {
            val nickname = event.getUser.getNick
            callback(ActionMessage(nickname, opUser.contains(nickname), event.getAction))
        }

        override def onPart(event: PartEvent[IRCBot])
        {
            if (showLeave) {
                callback(SystemMessage("[系統] %s 離開聊天室" format(event.getUser.getNick)))
            }
        }

        override def onJoin(event: JoinEvent[IRCBot])
        {
            val sender = event.getUser.getNick

            showJoin match {
                case true  => callback(SystemMessage("[系統] %s 加入聊天室" format(sender)))
                case false if (sender == nickname) => 
                    callback(SystemMessage("[系統] %s 加入聊天室" format(sender)))
                case _ => 
            }
        }

        override def onQuit(event: QuitEvent[IRCBot])
        {
            if (showLeave) {
                callback(SystemMessage("[系統] %s 離開聊天室" format(event.getUser.getNick)))
            }
        }

        override def onOp(event: OpEvent[IRCBot])
        {
            println("======== opOp =========")
            println("isOp:" + event.isOp)
            println("nickname:" + event.getRecipient.getNick)

            event.isOp match {
                case true => opUser += event.getRecipient.getNick
                case false => opUser -= event.getRecipient.getNick
            }
            println("opUser:" + opUser)
            println("=======================")

        }
    }

    override def log(line: String)
    {
        super.log(line)
        onLog(line)
    }

    def stop()
    {
        if (this.isConnected) {
            this.disconnect()
        }
    }

    private def connect()
    {
        password match {
            case None => super.connect(hostname, port)
            case Some(password) => super.connect(hostname, port, password)
        }
    }

    def start()
    {
        try {
            this.setAutoNickChange(true)
            this.setVerbose(true)
            this.setName(nickname)
            this.setEncoding("UTF-8")
            this.connect()
            this.getListenerManager.addListener(Callbacks)
            this.joinChannel(channel)
        } catch {
            case e: Exception => onError(e)
        }
    }

}


object IRCBot
{
    def doNothing(message: String) {}
    def doNothing(message: IRCMessage) {}
    def doNothing(exception: Exception) {
        println("From IRCBot.doNothing:")
        exception.printStackTrace()
    }
}
