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

class VoteWindow(parent: Shell) extends SWTHelper
{
  class VoteOption(label: Label, prompt: Text, removeButton: Button) {
    def dispose() {
      label.dispose()
      prompt.dispose()
      removeButton.dispose()
    }
  }

  val shell = new Shell(parent, SWT.APPLICATION_MODAL|SWT.DIALOG_TRIM)
  val gridLayout = new GridLayout(3, false)
  var options: List[String] = List("")
  var optionsWidget: List[VoteOption] = Nil

  val optionFrame = createGroup(shell, tr("Vote Options"), 3)
  var addButton: Button = null
  var startButton: Button = null
  var spinner: Spinner = null

  def createVoteOption(): (List[VoteOption], Button) = {
    
    optionsWidget.foreach(_.dispose())

    if (this.addButton != null) {
      this.addButton.dispose()
    }

    val newWidgets = for(i <- 0 until options.size) yield {

      val option = options(i)
      val label = new Label(optionFrame, SWT.NONE)
      val promptText = new Text(optionFrame, SWT.BORDER)
      val removeButton = new Button(optionFrame, SWT.PUSH)

      val gridData = new GridData(SWT.FILL, SWT.CENTER, true, false) 

      label.setText(i + ". ")
      promptText.setText(option)
      promptText.addModifyListener { e: ModifyEvent =>
        options = options.updated(i, promptText.getText.toString)
      }

      removeButton.setToolTipText(tr("Remove this option"))
      removeButton.setImage(MyIcon.remove)

      removeButton.addSelectionListener { e: SelectionEvent =>
        if (options.size > 1) {
          val (l1, l2) = options splitAt i
          options = l1 ::: (l2 drop 1)
          updateOptionArea(createVoteOption())
        }
      }

      promptText.setLayoutData(gridData)

      new VoteOption(label, promptText, removeButton)
    }

    val addButtonData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 3, 1)
    val addButton = new Button(optionFrame, SWT.PUSH)
    addButton.setLayoutData(addButtonData)
    addButton.setToolTipText(tr("Add vote option"))
    addButton.setImage(MyIcon.add)
    addButton.addSelectionListener { e: SelectionEvent =>
      options = options ++ List("")
      updateOptionArea(createVoteOption())
    }

    shell.layout(true, true)
    (newWidgets.toList, addButton)
  }

  def updateOptionArea(widgets: (List[VoteOption], Button)) {
    this.optionsWidget = widgets._1
    this.addButton = widgets._2
  }

  def isAllOptionsNotEmpty = options.forall(!_.isEmpty)
  def isIRCConnected = {

    try {

      implicit val timeout = Timeout(5.seconds)
      val status: Future[Boolean] = (MainWindow.controller ? IsConnected).mapTo[Boolean]
      Await.result(status, 5.seconds)
    } catch {
      case e: Exception => false
    }
  }

  def displayError(message: String) {
    val messageBox = new MessageBox(shell, SWT.ERROR|SWT.OK)
    messageBox.setMessage(message)
    messageBox.open()
  }

  def addStartButton(): Button = {
    val gridData = new GridData(SWT.RIGHT, SWT.FILL, true, false)
    val button = new Button(shell, SWT.PUSH)

    button.setText("Start")
    button.setImage(MyIcon.vote)
    button.setLayoutData(gridData)
    button.addSelectionListener { e: SelectionEvent =>

      println("Here...:" + isAllOptionsNotEmpty)

      if (!isAllOptionsNotEmpty) {
        displayError(tr("Vote options cannot have empty value."))
      } else if (!isIRCConnected) {
        displayError(tr("You need connect to IRC chatroom before start voting."))
      } else {
        MainWindow.controller ! StartVoting(options, this.spinner.getSelection)
        shell.dispose()
      }

    }
    button
  }

  def createDurationSpinner(): Spinner = 
  {
    val label = new Label(shell, SWT.LEFT)
    val spinner = new Spinner(shell, SWT.NONE)
    label.setText(tr("Vote duration (minutes):"))
    spinner.setMaximum(180)
    spinner.setMinimum(2)

    spinner
  }

  def open()
  {
    updateOptionArea(createVoteOption())
    this.spinner = createDurationSpinner()
    this.startButton = addStartButton()

    shell.setLayout(gridLayout)
    shell.setText(tr("Start Vote"))
    shell.setSize(600, 400)
    shell.open()
  }

}
