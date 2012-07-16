package org.bone.ircballoon

import org.pircbotx.PircBotX
import org.pircbotx.User
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events._

import scala.collection.JavaConversions._
import I18N.i18n._

/**
 *  IRC 機器人
 *
 *  @param  hostname    IRC Server
 *  @param  port        IRC Server Port
 *  @param  nickname    要使用的 IRC 暱稱
 *  @param  password    IRC Server 密碼
 *  @param  channel     要加入的 IRC 頻道
 *  @param  callback    機器人收到 IRC 來的訊息時的 Callback
 *  @param  onLog       當 IRCBot 進行 log 時的 Callback
 *  @param  onError     當錯誤發生時的 Callback
 *  @param  showJoin    是否顯示加入聊天室訊息
 *  @param  showLeave   是否顯示離開聊天室訊息
 */
class IRCBot(hostname: String, port: Int, nickname: String, 
             password: Option[String], channel: String, 
             callback: IRCMessage => Any,
             onLog: String => Any,
             onError: Exception => Any,
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
                val user = IRCUser(event.getUser.getNick)
                callback(SystemMessage(tr("[SYS] %s has left") format(user.nickname)))
            }
        }

        override def onJoin(event: JoinEvent[IRCBot])
        {
            val sender = event.getUser.getNick
            val user = IRCUser(event.getUser.getNick)

            showJoin match {
                case true  => 
                    callback(SystemMessage(tr("[SYS] %s has joined") format(user.nickname)))
                case false if (sender == nickname) => 
                    callback(SystemMessage(tr("[SYS] %s has joined") format(user.nickname)))
                case _ => 
            }
        }

        override def onQuit(event: QuitEvent[IRCBot])
        {
            if (showLeave) {
                callback(SystemMessage(tr("[SYS] %s has left") format(event.getUser.getNick)))
            }
        }
    }

    override def log(line: String)
    {
        super.log(line)
        onLog(line)
    }

    /**
     *  連線至 IRC Server
     */
    private def connect()
    {
        password match {
            case None => super.connect(hostname, port)
            case Some(password) => super.connect(hostname, port, password)
        }
    }

    /**
     *  停止 IRC 機器人
     */
    def stop()
    {
        runByThread {
            if (IRCBot.this.isConnected) {
                IRCBot.this.disconnect()
            }
        }
    }

    /**
     *  啟動 IRC 機器人
     */
    def start()
    {
        runByThread {
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

    def runByThread(action: => Any) 
    {
        val thread = new Thread()
        {
            override def run()
            {
                action
            }
        }

        thread.start()
    }

}


