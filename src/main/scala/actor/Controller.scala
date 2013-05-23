package org.bone.ircballoon.actor

import org.bone.ircballoon.MainWindow

import org.bone.ircballoon.actor.message._
import org.bone.ircballoon.actor.model._ 

import akka.actor._
import akka.event.Logging
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._

class ControllerActor extends Actor {

  private var ircBot: Option[IRCBot] = None
  private val notificationActor = context.actorOf(Props[NotificationActor])

  def stopBot() {
    future {
      ircBot.foreach(_.stopBot())
    }.onSuccess { 
      case _ => ircBot = None
    }
  }

  def startBot(info: IRCInfo) {
    ircBot = Some(new IRCBot(info, self))
    future { ircBot.foreach(_.startBot()) }
  }

  def sendMessage(message: String) = future {
    
    for {
      bot <- ircBot
      channel <- bot.getChannels.asScala
    } {
      val nickname = bot.getNick
      val user = bot.getUser(nickname)
      val isOP = user.getChannelsOpIn.contains(channel)
      bot.sendMessage(channel, message)
      notificationActor ! Message(message, IRCUser(nickname, isOP, isOP))
    }
  }

  val log = Logging(context.system, this)

  def receive = {
    case StartIRCBot(info) => startBot(info)
    case StopIRCBot => stopBot()
    case SendIRCMessage(message) => sendMessage(message)
    case m: NotificationMessage => notificationActor ! m
    case m: IRCMessage => notificationActor ! m
    case _ => log.info("Unknow message") 
  }

}

