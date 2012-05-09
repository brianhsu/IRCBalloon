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
                             displayTime: Int, fadeTime: Int,
                             spacing: Int) extends 
            Notification with NotificationBalloon
{
    private var currentNotification: List[BalloonWindow] = Nil

    def count = currentNotification.size
    private var isFull = false

    def addNotification(notification: BalloonWindow)
    {
        currentNotification = notification :: currentNotification

        if (notification.bottomY > (size._2 + location._2)) {
            isFull = true
        }
    }

    def removeNotification(finished: BalloonWindow) {
        currentNotification = currentNotification.filterNot(_.uid == finished.uid)
        finished.shell.dispose()

        if (currentNotification == Nil) {
            isFull = false
        }
    }

    def calculateLocationY = {
        currentNotification.map(_.bottomY) match {
            case Nil => location._2
            case xs  => xs.max + spacing
        }
    }

    def addMessage(message: String)
    {
        val thread = new Thread() {
            override def run() {
                
                Display.getDefault.syncExec(new Runnable() {
                    
                    while (isFull) {
                        Thread.sleep(200)
                    }

                    override def run() {
                        val balloon = new BalloonWindow(message)
                        balloon.open()
                    }
                })
            }
        }

        thread.start()
    }

    def open()
    {
    }

    def close()
    {
        currentNotification.foreach(_.shell.dispose())
    }
}

