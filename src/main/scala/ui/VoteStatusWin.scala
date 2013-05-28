package org.bone.ircballoon

import org.bone.ircballoon.model._
import org.bone.ircballoon.actor.message._

import I18N.i18n._
import ImageUtil._

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StackLayout
import org.eclipse.swt.custom.ScrolledComposite
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import org.eclipse.swt._
import scala.collection.mutable.ListBuffer

class VoteStatusWin(parent: Shell, candidates: List[String], duration: Int) extends SWTHelper
{
  case class VoteBar(label: Label, bar: ProgressBar, votes: Label)

  implicit val display = Display.getDefault

  var timeRemaining: Int = duration * 60
  var isVoting: Boolean = true

  val shell = new Shell(parent, SWT.DIALOG_TRIM|SWT.RESIZE)
  val candidateGroup = createGroup(shell, tr("Votes Status"), 3, 4)
  val gridLayout = new GridLayout(4, false)
  val voteBars: Vector[VoteBar] = createVoteBar(candidates)
  val timeLabel: Label = createTimeLabel()
  val resetTimeButton: Button = createResetTimeButton()
  val stopVoteButton: Button = createStopVoteButton()
  val closeButton: Button = createCloseButton()

  def displayFinishWindow()
  {
    val thread = new Thread() {
      override def run() {
        runByThread {
          val messageBox = new MessageBox(shell, SWT.OK|SWT.ICON_INFORMATION)
          SoundUtils.playSound("/sound/finish.wav")
          messageBox.setMessage(tr("Vote finsihed!"))
          messageBox.open()
        }
      }
    }
    thread.start()
  }

  def stopVote()
  {
    isVoting = false
    timeRemaining = 0
    timeLabel.setText(tr("Vote finished"))
    resetTimeButton.setEnabled(false)
    stopVoteButton.setEnabled(false)
    closeButton.setEnabled(true)
    displayFinishWindow()
  }
  
  def formatTime(timeInSeconds: Int): String = 
  {
    def addPrefixZero(value: Int) = if (value < 10) "0" + value.toString else value.toString

    val seconds = timeInSeconds % 60
    val minutes = timeInSeconds / 60

    addPrefixZero(minutes) + ":" + addPrefixZero(seconds)
  }

  def createTimeLabel(): Label = 
  {
    val label = new Label(shell, SWT.LEFT)
    label.setText(tr("Time Remaining: %s") format(formatTime(duration * 60)))
    label
  }

  def createCloseButton(): Button = 
  {
    val data = new GridData(SWT.RIGHT, SWT.FILL, false, false)
    val button = new Button(shell, SWT.PUSH)
    button.setLayoutData(data)
    button.setText(tr("Close"))
    button.setImage(MyIcon.close)
    button.addSelectionListener { e: SelectionEvent =>
      shell.dispose()
    }
    button.setEnabled(false)
    button
  }

  def createStopVoteButton(): Button =
  {
    val data = new GridData(SWT.RIGHT, SWT.FILL, false, false)
    val button = new Button(shell, SWT.PUSH)
    button.setLayoutData(data)
    button.setText(tr("Stop Vote"))
    button.addSelectionListener { e: SelectionEvent =>
      MainWindow.controller ! StopVoting
    }
    button
  }

  def createResetTimeButton(): Button =
  {
    val data = new GridData(SWT.RIGHT, SWT.FILL, true, false)
    val button = new Button(shell, SWT.PUSH)
    button.setLayoutData(data)
    button.setText(tr("Reset Time"))
    button.addSelectionListener { e: SelectionEvent =>
      MainWindow.controller ! ResetTime
      timeRemaining = duration * 60
      timeLabel.setText(tr("Time Remaining: %s") format(formatTime(timeRemaining)))
    }
    button.setEnabled(true)
    button
  }

  def createVoteBar(candidate: List[String]): Vector[VoteBar] = 
  {
    candidates.zipWithIndex.map { case (option, i) =>
      val barData = new GridData(SWT.FILL, SWT.NONE, true, false)
      val label = new Label(candidateGroup, SWT.LEFT)
      val bar = new ProgressBar(candidateGroup, SWT.SMOOTH|SWT.HORIZONTAL)
      val votes = new Label(candidateGroup, SWT.LEFT)

      label.setText(s"$i. $option")
      bar.setSelection(0)
      bar.setMaximum(100)
      bar.setLayoutData(barData)
      votes.setText(tr("%s votes") format(0))

      new VoteBar(label, bar, votes)
    }.toVector

  }

  def decreaseTimeLabel()
  {
    if (isVoting && timeRemaining > 0) {
      display.timerExec(1000, new Runnable() {
        override def run() {
          if (!timeLabel.isDisposed && timeRemaining > 0 && isVoting) {
            timeRemaining -= 1
            timeLabel.setText(tr("Time Remaining: %s") format(formatTime(timeRemaining)))
            decreaseTimeLabel()
          }
        }
      })
    }
  }

  def updateFinalVote(voteStatus: List[(String, Int)])
  {
    runByThread {
      updateVoteBar(voteStatus)
      stopVote()
    }
  }

  def updateVoteBar(voteStatus: List[(String, Int)])
  {
    runByThread {
      val totalVotes = voteStatus.map(_._2).sum.toDouble

      for (((voteTo, votes), i) <- voteStatus.zipWithIndex) {
        val percentage = ((votes / totalVotes) * 100).toInt
        voteBars(i).bar.setSelection(percentage)
        voteBars(i).votes.setText(tr("%s votes") format(votes))
      }
    }
  }

  def open()
  {
    shell.setLayout(gridLayout)
    shell.setText(tr("Vote Status"))
    shell.setSize(600, 400)
    shell.open()
    decreaseTimeLabel()
  }

}
