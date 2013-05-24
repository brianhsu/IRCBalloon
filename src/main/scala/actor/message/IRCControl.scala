package org.bone.ircballoon.actor.message

import org.bone.ircballoon.model.IRCUser
import org.bone.ircballoon.model.IRCInfo

sealed trait IRCControl

case class StartIRCBot(info: IRCInfo) extends IRCControl
case class SendIRCMessage(message: String) extends IRCControl
case object StopIRCBot extends IRCControl
case class IRCLog(line: String) extends IRCControl
case class IRCException(exception: Exception) extends IRCControl
case object CheckIRCAlive extends IRCControl

sealed trait VotingMessage
case class StartVoting(candidate: List[String], durationInMinute: Int) extends VotingMessage
case class Vote(user: IRCUser, voteTo: Int) extends VotingMessage
case object StopVoting extends VotingMessage
case class VoteResult(result: List[(String, Int)]) extends VotingMessage
case class VoteCurrent(result: List[(String, Int)]) extends VotingMessage

