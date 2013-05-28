package org.bone.ircballoon.actor.message

import org.bone.ircballoon.VoteStatusWin
import org.bone.ircballoon.model.IRCUser

sealed trait VotingMessage

case class StartVoting(
  candidate: List[String], durationInMinute: Int, 
  statusWindow: VoteStatusWin
) extends VotingMessage

case class Vote(user: IRCUser, voteTo: Int) extends VotingMessage
case object StopVoting extends VotingMessage
case class VoteResult(result: List[(String, Int)]) extends VotingMessage
case class VoteCurrent(result: List[(String, Int)]) extends VotingMessage
case object ResetTime extends VotingMessage

