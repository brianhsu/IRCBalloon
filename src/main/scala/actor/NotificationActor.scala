package org.bone.ircballoon.actor

import org.bone.ircballoon.Notification

import org.bone.ircballoon.actor.message._
import org.bone.ircballoon.actor.model._ 

import akka.actor._
import akka.event.Logging

class NotificationActor extends Actor {

  private val log = Logging(context.system, this)
  private var notification: Option[Notification] = None
  
  def openNotification(notification: Notification) {
    this.notification = Some(notification)
    this.notification.foreach(_.open())
  }

  def closeNotification() {
    println("closeNotification")
    this.notification.foreach(_.close())
    this.notification = None
  }

  def receive = {
    case SetNotification(notification) => openNotification(notification)
    case StopNotification => closeNotification()
    case ToggleNotification => notification.foreach(_.onTrayIconClicked())
    case m: Message => notification.foreach(_.addMessage(m))
    case _ => log.info("NotificationActor: Unknow message") 
  }

}

