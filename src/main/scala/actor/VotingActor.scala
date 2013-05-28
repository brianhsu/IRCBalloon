package org.bone.ircballoon.actor

import org.bone.ircballoon.MainWindow
import org.bone.ircballoon.SoundUtils
import org.bone.ircballoon.I18N.i18n._

import org.bone.ircballoon.actor.message._
import org.bone.ircballoon.model._ 

import akka.actor._
import akka.event.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import scala.collection.JavaConverters._

class VotingActor extends Actor {

  private val log = Logging(context.system, this)

  private var durationInMinutes: Int = 0
  private var stopTimer: Option[Cancellable] = None
  private var candidate: List[String] = Nil
  private var voteStatus: Map[String, Int] = Map()
  private var isVoting: Boolean = false

  def resetTime()
  {
    stopTimer.foreach(_.cancel)
    stopTimer = stopTimer.isDefined match {
      case false => None
      case true =>
        Some(
          context.system.scheduler.scheduleOnce(this.durationInMinutes.minutes) {
            context.parent ! StopVoting
          }
        )
    }
  }

  def startVoting(candidate: List[String], durationInMinutes: Int)
  {

    this.candidate = candidate
    this.voteStatus = Map()

    val candidateList: List[String] = candidate.zipWithIndex.map { case(name, index) => 
      s"  ${index}. $name" 
    }

    sender ! SendIRCMessage("============================")
    sender ! SendIRCMessage(tr("Start Voting!"))
    sender ! SendIRCMessage("")
    sender ! SendIRCMessage(tr("Vote duration is %d minutes").format(durationInMinutes))
    sender ! SendIRCMessage(tr("Candidate are:"))
    candidateList.foreach(m => sender ! SendIRCMessage(m))
    sender ! SendIRCMessage(tr("Please use format like 1++ to vote."))
    sender ! SendIRCMessage(tr("If you vote mutliple times, only last vote is counted."))
    sender ! SendIRCMessage("============================")

    this.isVoting = true

    this.durationInMinutes = durationInMinutes
    this.stopTimer = Some(
      context.system.scheduler.scheduleOnce(durationInMinutes.minutes) {
        context.parent ! StopVoting
      }
    )
  }

  def getVoteResult = {
    val votingCounting = voteStatus.groupBy(_._2).mapValues(_.size)

    candidate.zipWithIndex map { case(name, i) => 
      (name, votingCounting.getOrElse(i, 0)) 
    }
  }

  def vote(user: IRCUser, voteTo: Int) {
    if (isVoting && voteTo < candidate.size) {
      voteStatus = voteStatus.updated(user.nickname, voteTo).filter(_._2 < candidate.size)
      SoundUtils.playSound("/sound/vote.wav")
      sender ! VoteCurrent(getVoteResult)
    }
  }

  def stopVoting() {
    isVoting = false
    sender ! VoteResult(getVoteResult)
  }

  def receive = {
    case StartVoting(candidate, durationInMinute, _) => startVoting(candidate, durationInMinute)
    case StopVoting => stopVoting()
    case ResetTime => resetTime()
    case Vote(user, voteTo) => vote(user, voteTo)
    case x => log.info("votingActor/ Unknow message:" + x)
  }
}
