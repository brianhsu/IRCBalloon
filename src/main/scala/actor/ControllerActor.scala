package org.bone.ircballoon.actor

import org.bone.ircballoon.MainWindow
import org.bone.ircballoon.VoteStatusWin

import org.bone.ircballoon.actor.message._
import org.bone.ircballoon.model._ 

import akka.actor._
import akka.event.Logging

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import scala.collection.JavaConverters._

class ControllerActor extends Actor {

  private val log = Logging(context.system, this)

  private var ircBot: Option[IRCBot] = None
  private val notificationActor = context.actorOf(Props[NotificationActor])
  private val votingActor = context.actorOf(Props[VotingActor])

  private var voteStatusWin: Option[VoteStatusWin] = None

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

  def sendMessage(message: String) = {
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

  def updateVotingStatus(result: List[(String, Int)])
  {
    voteStatusWin.filterNot(_.shell.isDisposed).
                  foreach(_.updateVoteBar(result))
  }

  def showFinalVoting(result: List[(String, Int)]) 
  {
    
    def byVoting(x: ((String, Int), Int), y: ((String, Int), Int)): Boolean = {
      val ((xName, xVote), xNo) = x
      val ((yName, yVote), yNo) = y

      xVote > yVote
    }

    val sortedVote = result.zipWithIndex.sortWith(byVoting)

    self ! SendIRCMessage("======== 投票結果 ==========")

    sortedVote.foreach { case((name, vote), no) =>
      val plusSign = List.fill(vote)("+").mkString
      self ! SendIRCMessage(s"${no}. ${name}\t\t${plusSign}\t${vote} 票")
    }

    self ! SendIRCMessage("============================")
  }

  def startVoting(votingMessage: StartVoting)
  {
    this.voteStatusWin = Some(votingMessage.statusWindow)
    votingActor ! votingMessage
  }

  def receive = 
  {
    case StartIRCBot(info) => startBot(info)
    case StopIRCBot => stopBot()
    case CheckIRCAlive => checkIRCAlive()
    case IsConnected => sender ! (!ircBot.isEmpty && !ircBot.get.hasTimeouted)

    case SendIRCMessage(message) => sendMessage(message)

    case VoteCurrent(result) => updateVotingStatus(result)
    case VoteResult(result) => showFinalVoting(result)
    case v: StartVoting => startVoting(v)
    case v: VotingMessage => votingActor ! v

    case m: IRCMessage => notificationActor ! m
    case n: NotificationMessage => notificationActor ! n

    case IRCLog(line) => MainWindow.appendLog(line)
    case IRCException(exception) => {
      self ! StopIRCBot
      self ! StopNotification
      MainWindow.displayError(exception)
    }
    case _ => log.info("controllerActor/ Unknow message") 
  }

}


