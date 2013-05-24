package org.bone.ircballoon.actor

import org.bone.ircballoon.MainWindow

import org.bone.ircballoon.actor.message._
import org.bone.ircballoon.model._ 

import akka.actor._
import akka.event.Logging
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._
import scala.concurrent.duration._

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

    if (ircBot != None) {
      stopBot()
    }

    ircBot = Some(new IRCBot(info, self))
    future { 
      ircBot.foreach(_.startBot()) 
      self ! CheckIRCAlive
    }
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

  def checkIRCAlive() {
    
    ircBot.foreach { bot =>
      println("bot.hasTimeouted:" + bot.hasTimeouted)
      if (bot.hasTimeouted) {
        self ! SystemNotice("[SYS] IRC Disconnected. Pelease restart it again.")
      }
    }

    context.system.scheduler.scheduleOnce(180.seconds) {
      self ! CheckIRCAlive
    }

  }

  def receive = {
    case StartIRCBot(info) => startBot(info)
    case StopIRCBot => stopBot()
    case CheckIRCAlive => checkIRCAlive()
    case SendIRCMessage(message) => sendMessage(message)
    case m: NotificationMessage => notificationActor ! m
    case m: IRCMessage => notificationActor ! m
    case IRCLog(line) => MainWindow.appendLog(line)
    case IRCException(exception) => {
      self ! StopIRCBot
      self ! StopNotification
      MainWindow.displayError(exception)
    }
    case _ => log.info("Unknow message") 
  }

}

