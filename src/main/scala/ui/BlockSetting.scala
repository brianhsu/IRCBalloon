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

import java.io.File
import I18N.i18n._

class BlockSetting(tabFolder: TabFolder, parent: ScrolledComposite,
                   onModify: ModifyEvent => Any) extends Composite(parent, SWT.NONE) 
                                                 with SWTHelper
{
  var blockBackgroundImage: Option[String] = None

  val tabItem = new TabItem(tabFolder, SWT.NONE)

  var bgColor: Color = MyColor.Black
  var fontColor: Color = MyColor.White
  var borderColor: Color = MyColor.White
  var messageFont: Font = MyFont.DefaultFont
  var nicknameFont: Font = MyFont.DefaultFont
  var nicknameColor: Color = MyColor.White

  val alphaTitle = tr("Transparency:")

  val gridLayout = new GridLayout(4, false)

  val groupPos = createGroup(this, tr("Window Position / Size"))
  val groupBackground = createGroup(this, tr("Background Setting"))
  val groupMessageFont = createGroup(this, tr("Message Setting"))

  val locationX = createText(groupPos, tr("Window X:"))
  val locationY = createText(groupPos, tr("Window Y:"))
  val width = createText(groupPos, tr("Window Width:"))
  val height = createText(groupPos, tr("Window Height:"))

  val (borderLabel, borderButton) = createColorChooser(
    groupBackground, tr("Border Color:"), borderColor, borderColor = _
  )

  val (bgLabel, bgButton) = createColorChooser(
    groupBackground, tr("Background Color:"), bgColor, bgColor = _
  )

  val (bgImageLabel, bgImageButton, bgImageCheck) = createBackgroundImage(groupBackground)

  val (nicknameColorLabel, nicknameColorButton) = createColorChooser(
    groupMessageFont, tr("Nickname Color:"), nicknameColor, nicknameColor = _
  )

  val (nicknameFontLabel, nicknameFontButton) = createFontChooser(
    groupMessageFont, tr("Nickname Font:"), 
    nicknameFont,
    nicknameFont = _
  )

  val (fgLabel, fgButton) = createColorChooser(groupMessageFont, tr("Message Color:"), fontColor, fontColor = _)
  val (fontLabel, fontButton) = createFontChooser(
      groupMessageFont, tr("Message Font:"), 
      messageFont,
      messageFont = _
  )

  val scrollBarCheckbox = createCheckBox(groupMessageFont, tr("Show scroll bar"))
  val (messageSizeLabel, messageSizeSpinner) = createSpinner(groupMessageFont, tr("Message Limit:"), 1, 50)

  val (transparentLabel, transparentScale) = createScaleChooser(this, alphaTitle)
  val previewButton = createPreviewButton()
  val noticeLabel = createNoticeLabel()

  def setBlockBackgroundImage(imageFile: String) 
  {
    bgImageButton.setToolTipText(imageFile)
    bgImageButton.setText((new File(imageFile)).getName)
    blockBackgroundImage = Some(imageFile)        
  }

  def createBackgroundImage(parent: Composite) = 
  {
    val layoutData = new GridData(SWT.FILL, SWT.FILL, true, false)
    val layoutData2 = new GridData(SWT.FILL, SWT.FILL, true, false)
    layoutData2.horizontalSpan = 2

    val label = new Label(parent, SWT.LEFT)
    val browse = new Button(parent, SWT.PUSH)
    val clear = new Button(parent, SWT.PUSH)

    label.setText(tr("Background Image"))
    clear.setLayoutData(layoutData2)
    clear.setText(tr("Remove Background Image"))
    clear.addSelectionListener { e: SelectionEvent =>
      browse.setText(tr("Browse..."))
      browse.setToolTipText("")
      blockBackgroundImage = None
    }

    browse.setLayoutData(layoutData)
    browse.setText(tr("Browse..."))
    browse.addSelectionListener { e: SelectionEvent =>
      val extensions = Array("*.jpeg;*.jpg;*.png;*.gif;*.JPG;*.PNG;*.GIF;*.JPEG")
      val fileDialog = new FileDialog(MainWindow.shell, SWT.OPEN)

      fileDialog.setFilterExtensions(extensions)

      val imageFile = fileDialog.open()

      if (imageFile != null) {
        setBlockBackgroundImage(imageFile)
      }
    }

    (label, browse, clear)
  }

  def createNoticeLabel() =
  {
    val layoutData = new GridData(SWT.LEFT, SWT.CENTER, true, false)
    val label = new Label(this, SWT.LEFT|SWT.WRAP)
    layoutData.horizontalSpan =4
    label.setLayoutData(layoutData)
    label.setText(tr("* Drag and drop the right-bottom corner of chat window to resize"))
    label
  }

  class TestThread(notificationBlock: NotificationBlock) extends Thread
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
        notificationBlock.addMessage(SystemNotice("[%d] %s" format(count, message)))
        count = (count + 1)
        Thread.sleep(1000)
      }
    }
  }

  def createNotificationBlock() = 
  {
    val size = (width.getText.toInt, height.getText.toInt)
    val location = (locationX.getText.toInt, locationY.getText.toInt)
    val messageSize = messageSizeSpinner.getSelection
    val alpha = 255 - (255 * (transparentScale.getSelection / 100.0)).toInt

    NotificationBlock(
      size, location, 
      borderColor, bgColor, alpha, 
      fontColor, messageFont, 
      nicknameColor, nicknameFont,
      messageSize, scrollBarCheckbox.getSelection,
      blockBackgroundImage,
      showTimestamp = true
    )
  }

  def setDefaultValue()
  {
    locationX.setText("100")
    locationY.setText("100")
    width.setText("300")
    height.setText("500")
    messageSizeSpinner.setSelection(10)
  }

  def setTextVerify()
  {
    locationX.addVerifyListener { e: VerifyEvent => e.doit = e.text.forall(_.isDigit) }
    locationY.addVerifyListener { e: VerifyEvent => e.doit = e.text.forall(_.isDigit) }
    width.addVerifyListener { e: VerifyEvent => e.doit = e.text.forall(_.isDigit) }
    height.addVerifyListener { e: VerifyEvent => e.doit = e.text.forall(_.isDigit) }
  }

  def createPreviewButton() =
  {
    var notificationBlock: Option[NotificationBlock] = None
    var testThread: Option[TestThread] = None

    val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
    val button = new Button(this, SWT.PUSH)

    def startPreview ()
    {
      notificationBlock = Some(createNotificationBlock)
      notificationBlock.foreach{ block => 
        block.open()
        testThread = Some(new TestThread(block))
        testThread.foreach(_.start)
      }
      button.setText(tr("Stop Preview"))
    }

    def stopPreview()
    {
      button.setText(tr("Start Preview"))
      notificationBlock.foreach{ block =>
        testThread.foreach{_.setStop(true)}
        testThread = None
        block.close()
      }
      notificationBlock = None
    }

    layoutData.horizontalSpan = 2
    button.setLayoutData(layoutData)
    button.setText(tr("Start Preview"))
    button.addSelectionListener { e: SelectionEvent =>
      notificationBlock match {
        case None    => startPreview()
        case Some(x) => stopPreview()
      }
    }
    button
  }

  def isSettingOK = {
    locationX.getText.trim.length > 0 &&
    locationY.getText.trim.length > 0 &&
    width.getText.trim.length > 0 &&
    height.getText.trim.length > 0
  }

  def updatePreviewButtonState(e: ModifyEvent)
  {
    previewButton.setEnabled(isSettingOK)
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
    bgButton.setEnabled(isEnabled)
    fgButton.setEnabled(isEnabled)
    fontButton.setEnabled(isEnabled)
    transparentScale.setEnabled(isEnabled)
    messageSizeSpinner.setEnabled(isEnabled)
    previewButton.setEnabled(isEnabled)

    borderButton.setEnabled(isEnabled)
    bgImageButton.setEnabled(isEnabled)
    bgImageCheck.setEnabled(isEnabled)

    nicknameColorButton.setEnabled(isEnabled)
    nicknameFontButton.setEnabled(isEnabled)
    transparentScale.setEnabled(isEnabled)
    parent.setEnabled(isEnabled)
  }

  def resetScrollSize()
  {
    val r = parent.getClientArea();
    parent.setMinSize(BlockSetting.this.computeSize(r.width, SWT.DEFAULT))
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

  this.tabItem.setText(tr("Pinned Chat Window"))
  this.tabItem.setControl(parent)
  this.resetScrollSize()
}

