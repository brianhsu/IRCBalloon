package org.bone.ircballoon.actor.model

case class IRCInfo(hostname: String, port: Int, nickname: String, channel: String, password: Option[String] = None)
case class IRCUser(nickname: String, isOP: Boolean, isBroadcaster: Boolean)

