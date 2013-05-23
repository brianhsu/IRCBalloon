package org.bone.ircballoon.actor.message

import org.bone.ircballoon.Notification

trait NotificationMessage
case class SetNotification(notification: Notification) extends NotificationMessage
case object StopNotification extends NotificationMessage
case object ToggleNotification extends NotificationMessage
