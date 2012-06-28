package org.bone.ircballoon

import org.pircbotx.PircBotX
import org.pircbotx.User
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events._

import scala.collection.JavaConversions._

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
        private def isBrodcaster(user: User): Boolean = 
        {
            hostname == ("%s.jtvirc.com" format(user.getNick)) &&
            channel  == ("#%s" format(user.getNick))
        }

        private def isOp(user: User): Boolean =
        {
            isBrodcaster(user) || user.getChannelsOpIn.map(_.getName).contains(channel)
        }

        override def onMessage(event: MessageEvent[IRCBot])
        {
            val nickname = event.getUser.getNick
            callback(ChatMessage(nickname, isOp(event.getUser), event.getMessage))
        }

        override def onAction(event: ActionEvent[IRCBot])
        {
            val nickname = event.getUser.getNick
            callback(ActionMessage(nickname, isOp(event.getUser), event.getAction))
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
        val thread = new Thread()
        {
            override def run()
            {
                if (IRCBot.this.isConnected) {
                    IRCBot.this.disconnect()
                }
            }
        }

        thread.start()
    }

    def start()
    {
        val thread = new Thread()
        {
            override def run()
            {
                try {
                    IRCBot.this.setAutoNickChange(true)
                    IRCBot.this.setVerbose(true)
                    IRCBot.this.setName(nickname)
                    IRCBot.this.setEncoding("UTF-8")
                    IRCBot.this.connect()
                    IRCBot.this.getListenerManager.addListener(Callbacks)
                    IRCBot.this.joinChannel(channel)
                } catch {
                    case e: Exception => onError(e)
                }
            }
        }

        thread.start()
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
