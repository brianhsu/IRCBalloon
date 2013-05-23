package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText

import org.eclipse.swt._
import scala.math._

case class BalloonController(size: (Int, Int), location: (Int, Int), 
                             borderColor: Color, bgColor: Color, alpha: Int,
                             fontColor: Color, font: Font,
                             nicknameColor: Color, nicknameFont: Font,
                             displayTime: Int, fadeTime: Int,
                             spacing: Int) extends 
            Notification with NotificationBalloon with SWTHelper
{

  private implicit val display = Display.getDefault

  private var currentNotification: List[BalloonWindow] = Nil
  private var waitNotification: List[BalloonWindow] = Nil

  override def onTrayIconClicked()
  {
    currentNotification.foreach { notification =>
      runByThread {
        if (!notification.shell.isDisposed) {
          notification.shell.setVisible(!notification.shell.getVisible)
        }
      }
    }
  }

  def reLocationBalloons(waitBalloons: List[BalloonWindow]) =
  {
    var lastY = location._2
    var relocated: List[BalloonWindow] = Nil

    waitBalloons.reverse.foreach { balloon =>
      balloon.shell.setLocation(location._1, lastY)
      lastY = balloon.bottomY
      relocated ::= balloon
    }

    relocated
  }

  def removeNotification(finished: BalloonWindow) {
    currentNotification = currentNotification.filterNot(_.uid == finished.uid)

    if (currentNotification == Nil) {
      val relocated = reLocationBalloons(waitNotification)
      val (inRange, outRange) = relocated.partition(_.bottomY <= location._2 + size._2)

      currentNotification = inRange
      waitNotification = outRange

      // 如果等待的列表裡面沒有符合通知區域大小的東西，那就
      // 直接顯示時間點最前面的那個。
      if (currentNotification == Nil && waitNotification != Nil) {
        currentNotification ::= waitNotification.last 
        waitNotification = waitNotification.dropRight(1)
      }

      currentNotification.foreach(_.open())
    }

    finished.shell.dispose()
  }

  def calculateLocationY = {
    (currentNotification ++ waitNotification).map(_.bottomY) match {
      case Nil => location._2
      case xs  => xs.max
    }
  }

  def addMessage(message: IRCMessage)
  {
    runByThread {
      val notification = new BalloonWindow(location, bgColor, borderColor, message)
      notification.prepare()

      val locationY = calculateLocationY
      val newBottom = locationY + notification.shell.getSize.y
      val bottomLine = location._2 + size._2

      notification.shell.setLocation(location._1, locationY)

      if (newBottom <= bottomLine || currentNotification == Nil) {
        currentNotification = notification :: currentNotification
        notification.open()
      } else {
        waitNotification = notification :: waitNotification
      }
    }
  }

  def open() { }

  def close()
  {
    currentNotification.foreach(_.close())
  }
}

