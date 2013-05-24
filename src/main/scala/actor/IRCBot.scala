package org.bone.ircballoon.actor

import org.bone.ircballoon.actor.message._
import org.bone.ircballoon.model._ 

import akka.actor._

import org.pircbotx._
import org.pircbotx.hooks._
import org.pircbotx.hooks.events._

import scala.collection.JavaConverters._

class IRCBot(info: IRCInfo, controllerActor: ActorRef) extends PircBotX {

  private var isWatching: Boolean = false
  private var lastServerResponseTime: Long = System.currentTimeMillis

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
      
      controllerActor ! Message(event.getMessage, toIRCUser(event.getUser, event.getChannel))

      if (event.getMessage == "aaa") {
        IRCBot.this.sendCTCPCommand(event.getChannel, "PING")
      }

      val votingRegex = """(\d+)\+\+""".r
      val vote = votingRegex.findAllMatchIn(event.getMessage).map(_.group(0).dropRight(2)).toList
      vote.foreach { voteTo =>
        controllerActor ! Vote(toIRCUser(event.getUser, event.getChannel), voteTo.toInt)
      }
    }

    override def onAction(event: ActionEvent[IRCBot]) {
      controllerActor ! Action(event.getAction, toIRCUser(event.getUser, event.getChannel))
    }

    override def onPart(event: PartEvent[IRCBot]) {
      if (info.showLeave) {
        controllerActor ! Part(event.getReason, event.getChannel.getName, toIRCUser(event.getUser, event.getChannel))
      }
    }

    override def onJoin(event: JoinEvent[IRCBot]) {
      if (info.showJoin || event.getUser.getNick == info.nickname) {
        controllerActor ! Join(event.getChannel.getName, toIRCUser(event.getUser, event.getChannel))
      }
    }

    override def onQuit(event: QuitEvent[IRCBot]) {
      if (info.showLeave) {
        controllerActor ! Quit(IRCUser(event.getUser.getNick, false, false), event.getReason)
      }
    }
  }
  
  override def handleLine(line: String) {
    super.handleLine(line)
    this.lastServerResponseTime = System.currentTimeMillis
  }

  override def log(line: String) {
    super.log(line)
    controllerActor ! IRCLog(line)
  }

  def hasTimeouted = ((System.currentTimeMillis - this.lastServerResponseTime) / 1000) > 60 * 10

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
    try {
      IRCBot.this.setAutoNickChange(true)
      IRCBot.this.setVerbose(true)
      IRCBot.this.setName(info.nickname)
      IRCBot.this.setEncoding("UTF-8")
      IRCBot.this.connect()
      IRCBot.this.getListenerManager.addListener(Handlers)
      IRCBot.this.joinChannel(info.channel)
      IRCBot.this.setSocketTimeout(1000 * 30)
    } catch {
      case e: Exception => controllerActor ! IRCException(e)
    }
  }
}

