package org.bone.ircballoon.actor

import org.bone.ircballoon.MainWindow

import org.bone.ircballoon.actor.message._
import org.bone.ircballoon.model._ 

import akka.actor._
import akka.event.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import scala.collection.JavaConverters._
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

class VotingActor extends Actor {

  private val log = Logging(context.system, this)

  private var durationInMinutes: Int = 0
  private var stopTimer: Option[Cancellable] = None
  private var candidate: List[String] = Nil
  private var voteStatus: Map[String, Int] = Map()
  private var isVoting: Boolean = false

  def playSound()
  {
    val thread = new Thread() {
      override def run() {
        val audioIn = AudioSystem.getAudioInputStream(getClass.getResource("/vote.wav"))
        val clip = AudioSystem.getClip()
        clip.open(audioIn)
        clip.start()
      }
    }
    thread.start()
  }

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

    val candidateListing: List[String] = candidate.zipWithIndex.map { case(name, index) => s"  ${index}. $name" }
    val messages = List(
      "============================",
      "開始投票！",
      "",
      s"投票時間為 ${durationInMinutes} 分鐘，選項如下："
    ) ++ candidateListing ++ List(
      "請使用 1++ 此格式投票，重覆投票以最後一票計",
      "============================"
    )

    messages.foreach(m => sender ! SendIRCMessage(m))
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
      playSound()
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
