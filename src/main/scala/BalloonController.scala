package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText

import org.eclipse.swt._
import scala.math._

class BalloonController extends Notification with NotificationBalloon
{
    private var currentNotification: List[BalloonWindow] = Nil

    def count = currentNotification.size
    def isFull: Boolean = {
        currentNotification match {
            case Nil => false
            case xs  => xs.last.bottomY >= 300
        }
    }

    def addNotification(notification: BalloonWindow)
    {
        currentNotification = notification :: currentNotification
    }

    def removeNotification(finished: BalloonWindow) {
        currentNotification = currentNotification.filterNot(_.uid == finished.uid)
        finished.shell.dispose()
    }

    def calculateLocationY = {
        currentNotification.map(_.bottomY) match {
            case Nil => 100
            case xs  => xs.max + 10
        }
    }

    def addMessage(message: String)
    {
        val thread = new Thread() {
            override def run() {
                
                Display.getDefault.syncExec(new Runnable() {
                    
                    println("prepare insert:" + message)
                    println("isFull:" + isFull)
                    println("xs:" + currentNotification.map(_.sn))

                    while (isFull) {
                        println("睡 0.2 秒")
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
    }
}

