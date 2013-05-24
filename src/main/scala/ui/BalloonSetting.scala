package org.bone.ircballoon

import org.bone.ircballoon.actor.message._

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StackLayout
import org.eclipse.swt.custom.ScrolledComposite

import org.eclipse.swt._

import scala.util.Random
import I18N.i18n._

class BalloonSetting(tabFolder: TabFolder, parent: ScrolledComposite, 
                     onModify: ModifyEvent => Any) extends Composite(parent, SWT.NONE) 
                                                   with SWTHelper
{
  val tabItem = new TabItem(tabFolder, SWT.NONE)

  var bgColor: Color = MyColor.Black
  var fontColor: Color = MyColor.White
  var nicknameColor: Color = MyColor.White
  var borderColor: Color = MyColor.White
  var messageFont: Font = MyFont.DefaultFont
  var nicknameFont: Font = MyFont.DefaultFont

  val alphaTitle = tr("Transparency:")

  val gridLayout = new GridLayout(4, false)

  val areaGroup = createGroup(this, tr("Notification Area Setting"))
  val locationX = createText(areaGroup, tr("Area X:"))
  val locationY = createText(areaGroup, tr("Area Y:"))
  val width = createText(areaGroup, tr("Area Width:"))
  val height = createText(areaGroup, tr("Area Height:"))
  val areaSpan = createSpanLabel(areaGroup, 2)
  val areaSelectionButton = createAreaSelectionButton(areaGroup)

  val backgroundGroup = createGroup(this, tr("Background Setting"))

  val (borderLabel, borderButton) = createColorChooser(
    backgroundGroup, tr("Border Color:"), 
    borderColor, borderColor = _
  )

  val (bgLabel, bgButton) = createColorChooser(
    backgroundGroup, tr("Background Color:"), 
    bgColor, bgColor = _
  )

  val fontGroup = createGroup(this, tr("Message Setting"))
  val (nicknameColorLabel, nicknameColorButton) = createColorChooser(
    fontGroup, tr("Nickname Color:"),
    nicknameColor, nicknameColor = _
  )

  val (nicknameFontLabel, nicknameFontButton) = createFontChooser(
    fontGroup, tr("Nickname Font:"),
    nicknameFont,
    nicknameFont = _
  )

  val (fgLabel, fgButton) = createColorChooser(fontGroup, tr("Message Color:"), fontColor, fontColor = _)
  val (fontLabel, fontButton) = createFontChooser(fontGroup, tr("Message Font:"), messageFont, messageFont = _)
  val effectGroup = createGroup(this, tr("Effect Setting"))
  val (transparentLabel, transparentScale) = createScaleChooser(effectGroup, alphaTitle)
  val (displayTimeLabel, displayTimeSpinner) = createSpinner(effectGroup, tr("Bubble keeps (second):"), 1, 120)
  val (fadeTimeLabel, fadeTimeSpinner) = createSpinner(effectGroup, tr("Animation time (ms):"), 1, 5000)
  val (spacingLabel, spacingSpinner) = createSpinner(effectGroup, tr("Space between bubbles:"), 1, 20)

  val spanLabel = createSpanLabel(this, 1)
  val previewButton = createPreviewButton()

  class TestThread(balloonController: BalloonController) extends Thread
  {
    private var shouldStop = false

    def setStop(shouldStop: Boolean)
    {
      this.shouldStop = shouldStop
    }

    override def run ()
    {
      var count = 1

      while (!shouldStop) {
        val message = MessageSample.random(1).head

        balloonController.addMessage(SystemNotice("[%d] %s" format(count, message)))

        count = (count + 1)
        Thread.sleep(1000)
      }
    }
  }

  def createBalloonController() = 
  {
    val size = (width.getText.toInt, height.getText.toInt)
    val location = (locationX.getText.toInt, locationY.getText.toInt)
    val alpha = 255 - (255 * (transparentScale.getSelection / 100.0)).toInt

    BalloonController(
      size, location, 
      borderColor, bgColor, alpha, 
      fontColor, messageFont, 
      nicknameColor, nicknameFont,
      displayTimeSpinner.getSelection * 1000, 
      fadeTimeSpinner.getSelection,
      spacingSpinner.getSelection,
      showTimestamp = true
    )
  }

  def setDefaultValue()
  {
    locationX.setText("100")
    locationY.setText("100")
    width.setText("300")
    height.setText("300")
    displayTimeSpinner.setSelection(5)
    fadeTimeSpinner.setSelection(500)
    spacingSpinner.setSelection(5)
  }

  def setTextVerify()
  {
    locationX.addVerifyListener { e: VerifyEvent => e.doit = e.text.forall(_.isDigit) }
    locationY.addVerifyListener { e: VerifyEvent => e.doit = e.text.forall(_.isDigit) }
    width.addVerifyListener { e: VerifyEvent => e.doit = e.text.forall(_.isDigit) }
    height.addVerifyListener { e: VerifyEvent => e.doit = e.text.forall(_.isDigit) }
  }

  def setNotificationArea(x: Int, y: Int, width: Int, height: Int)
  {
    this.locationX.setText(x.toString)
    this.locationY.setText(y.toString)
    this.width.setText(width.toString)
    this.height.setText(height.toString)
  }

  def createAreaSelectionButton(parent: Composite) =
  {
    val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
    val button = new Button(parent, SWT.PUSH)
    layoutData.horizontalSpan = 2
    button.setLayoutData(layoutData)
    button.setText(tr("Select notification Area"))
    button.addSelectionListener { e: SelectionEvent =>
      def oldArea = (locationX.getText.toInt, locationY.getText.toInt, width.getText.toInt, height.getText.toInt)
      val areaSelection = new AreaSelectionDialog(oldArea, setNotificationArea _)
      areaSelection.open()
    }

    button
  }

  def createPreviewButton() =
  {
    var balloonController: Option[BalloonController] = None
    var testThread: Option[TestThread] = None

    val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
    val button = new Button(this, SWT.PUSH)

    def startPreview ()
    {
      balloonController = Some(createBalloonController)
      balloonController.foreach{ controller => 
        controller.open()
        testThread = Some(new TestThread(controller))
        testThread.foreach(_.start)
      }
      button.setText(tr("Stop Preview"))
    }

    def stopPreview()
    {
      button.setText(tr("Start Preview"))
      balloonController.foreach{ controller =>
        testThread.foreach{_.setStop(true)}
        testThread = None
        controller.close()
      }

      balloonController = None
    }

    layoutData.horizontalSpan = 2
    button.setLayoutData(layoutData)
    button.setText(tr("Start Preview"))
    button.addSelectionListener { e: SelectionEvent =>
      balloonController match {
        case None    => startPreview()
        case Some(x) => stopPreview()
      }
    }
    button
  }

  def updatePreviewButtonState(e: ModifyEvent)
  {
    previewButton.setEnabled(isSettingOK)
  }

  def isSettingOK = {
    locationX.getText.trim.length > 0 &&
    locationY.getText.trim.length > 0 &&
    width.getText.trim.length > 0 &&
    height.getText.trim.length > 0
  }

  def setModifyListener() 
  {
    locationX.addModifyListener(onModify)
    locationY.addModifyListener(onModify)
    width.addModifyListener(onModify)
    height.addModifyListener(onModify)

    locationX.addModifyListener(updatePreviewButtonState _)
    locationY.addModifyListener(updatePreviewButtonState _)
    width.addModifyListener(updatePreviewButtonState _)
    height.addModifyListener(updatePreviewButtonState _)
  }

  def setUIEnabled(isEnabled: Boolean)
  {
    locationX.setEnabled(isEnabled)
    locationY.setEnabled(isEnabled)
    width.setEnabled(isEnabled)
    height.setEnabled(isEnabled)
    areaSelectionButton.setEnabled(isEnabled)
    borderButton.setEnabled(isEnabled)
    nicknameFontButton.setEnabled(isEnabled)
    nicknameColorButton.setEnabled(isEnabled)
    bgButton.setEnabled(isEnabled)
    fgButton.setEnabled(isEnabled)
    fontButton.setEnabled(isEnabled)
    transparentScale.setEnabled(isEnabled)
    displayTimeSpinner.setEnabled(isEnabled)
    fadeTimeSpinner.setEnabled(isEnabled)
    spacingSpinner.setEnabled(isEnabled)
    previewButton.setEnabled(isEnabled)
  }

  def resetScrollSize()
  {
    val r = parent.getClientArea();
    parent.setMinSize(BalloonSetting.this.computeSize(r.width, SWT.DEFAULT))
  }

  this.setLayout(gridLayout)
  this.setDefaultValue()
  this.setTextVerify()
  this.setModifyListener()

  this.parent.setContent(this)
  this.parent.setExpandVertical(true)
  this.parent.setExpandHorizontal(true)

  this.parent.addControlListener(new ControlAdapter() {
    override def controlResized(e: ControlEvent) {
      resetScrollSize()
    }
  })

  this.tabItem.setText(tr("Bubble Notification"))
  this.tabItem.setControl(parent)
}

