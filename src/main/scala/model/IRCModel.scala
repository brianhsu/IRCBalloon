package org.bone.ircballoon.model

case class IRCInfo(
  hostname: String, port: Int, nickname: String, 
  channel: String, password: Option[String] = None, 
  showJoin: Boolean, showLeave: Boolean
)

case class IRCUser(nickname: String, isOP: Boolean, isBroadcaster: Boolean)

