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

class VoteStatusWin(parent: Shell, candidates: List[String]) extends SWTHelper
{
  case class VoteBar(label: Label, bar: ProgressBar, votes: Label)

  implicit val display = Display.getDefault
  val shell = new Shell(parent, SWT.APPLICATION_MODAL|SWT.DIALOG_TRIM)
  val gridLayout = new GridLayout(3, false)
  val voteBars: Vector[VoteBar] = createVoteBar(candidates)

  def createVoteBar(candidate: List[String]): Vector[VoteBar] = {
    
    candidates.zipWithIndex.map { case (option, i) =>
      val barData = new GridData(SWT.FILL, SWT.NONE, true, false)
      val label = new Label(shell, SWT.LEFT)
      val bar = new ProgressBar(shell, SWT.SMOOTH|SWT.HORIZONTAL)
      val votes = new Label(shell, SWT.LEFT)

      label.setText(s"$i. $option")
      bar.setSelection(0)
      bar.setMaximum(100)
      bar.setLayoutData(barData)
      votes.setText(tr("%s votes") format(0))

      new VoteBar(label, bar, votes)
    }.toVector

  }

  def updateFinalVote(voteStatus: List[(String, Int)])
  {
    updateVoteBar(voteStatus)
  }

  def updateVoteBar(voteStatus: List[(String, Int)])
  {
    runByThread {
      println("voteStatus in status win:" + voteStatus)

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
    shell.setText(tr("Start Vote"))
    shell.setSize(600, 400)
    shell.open()
  }

}
