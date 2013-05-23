package org.bone.ircballoon.actor

import org.bone.ircballoon.actor.message._
import org.bone.ircballoon.model._ 

import akka.actor._

import org.pircbotx._
import org.pircbotx.hooks._
import org.pircbotx.hooks.events._

import scala.collection.JavaConverters._

class IRCBot(info: IRCInfo, ircActor: ActorRef) extends PircBotX {

  object Handlers extends ListenerAdapter[IRCBot] {

    private def isBroadcaster(user: User): Boolean = {
      info.hostname == s"${user.getNick}.jtvirc.com" &&
      info.channel == s"#${user.getNick}"
    }

    private def isOP(user: User, channel: Channel): Boolean = {
      isBroadcaster(user) || channel.getOps.asScala.map(_.getNick).contains(user.getNick)
    }

    private def toIRCUser(user: User, channel: Channel) = {
      IRCUser(user.getNick, isOP(user, channel), isBroadcaster(user))
    }

    override def onMessage(event: MessageEvent[IRCBot]) {
      ircActor ! Message(event.getMessage, toIRCUser(event.getUser, event.getChannel))
    }

    override def onAction(event: ActionEvent[IRCBot]) {
      ircActor ! Action(event.getAction, toIRCUser(event.getUser, event.getChannel))
    }

    override def onPart(event: PartEvent[IRCBot]) {
      if (info.showLeave) {
        ircActor ! Part(event.getReason, event.getChannel.getName, toIRCUser(event.getUser, event.getChannel))
      }
    }

    override def onJoin(event: JoinEvent[IRCBot]) {
      if (info.showJoin || event.getUser.getNick == info.nickname) {
        ircActor ! Join(event.getChannel.getName, toIRCUser(event.getUser, event.getChannel))
      }
    }

    override def onQuit(event: QuitEvent[IRCBot]) {
      if (info.showLeave) {
        ircActor ! Quit(IRCUser(event.getUser.getNick, false, false), event.getReason)
      }
    }
  }
  
  def connect()
  {
    info.password match {
      case None => super.connect(info.hostname, info.port)
      case Some(password) => super.connect(info.hostname, info.port, password)
    }
  }
  
  def stopBot() {
    IRCBot.this.quitServer()
  }

  def startBot() {
    IRCBot.this.setAutoNickChange(true)
    IRCBot.this.setVerbose(true)
    IRCBot.this.setName(info.nickname)
    IRCBot.this.setEncoding("UTF-8")
    IRCBot.this.connect()
    IRCBot.this.getListenerManager.addListener(Handlers)
    IRCBot.this.joinChannel(info.channel)
  }
}

