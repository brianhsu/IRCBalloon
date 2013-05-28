package org.bone.ircballoon.actor

import org.bone.ircballoon.MainWindow

import org.bone.ircballoon.actor.message._
import org.bone.ircballoon.model._ 

import akka.actor._
import akka.event.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import scala.collection.JavaConverters._

class VotingActor extends Actor {

  private val log = Logging(context.system, this)

  private var candidate: List[String] = Nil
  private var voteStatus: Map[String, Int] = Map()
  private var isVoting: Boolean = false

  def startVoting(candidate: List[String], durationInMinute: Int)
  {

    this.candidate = candidate
    this.voteStatus = Map()

    val candidateListing: List[String] = candidate.zipWithIndex.map { case(name, index) => s"  ${index}. $name" }
    val messages = List(
      "============================",
      "開始投票！",
      "",
      s"投票時間為 ${durationInMinute} 分鐘，選項如下："
    ) ++ candidateListing ++ List(
      "請使用 1++ 此格式投票，重覆投票以最後一票計",
      "============================"
    )

    messages.foreach(m => sender ! SendIRCMessage(m))
    this.isVoting = true

    context.system.scheduler.scheduleOnce(durationInMinute.minutes) {
      context.parent ! StopVoting
    }
  }

  def getVoteResult = {
    val votingCounting = voteStatus.groupBy(_._2).mapValues(_.size)

    candidate.zipWithIndex map { case(name, i) => 
      (name, votingCounting.getOrElse(i, 0)) 
    }
  }

  def vote(user: IRCUser, voteTo: Int) {
    if (isVoting) {
      voteStatus = voteStatus.updated(user.nickname, voteTo).filter(_._2 < candidate.size)
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
    case Vote(user, voteTo) => vote(user, voteTo)
    case x => log.info("votingActor/ Unknow message:" + x)
  }
}
