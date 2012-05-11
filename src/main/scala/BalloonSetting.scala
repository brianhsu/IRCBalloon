package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StackLayout

import org.eclipse.swt._

import scala.util.Random

class BalloonSetting(parent: TabFolder, onModify: ModifyEvent => Any) extends 
      Composite(parent, SWT.NONE) with SWTHelper
{
    val tabItem = new TabItem(parent, SWT.NONE)
    var bgColor: Color = MyColor.Black
    var fontColor: Color = MyColor.White
    var borderColor: Color = MyColor.White
    var messageFont: Font = Display.getDefault.getSystemFont

    val alphaTitle = "透明度："

    val gridLayout = new GridLayout(4, false)
    val locationX = createText(this, "通知區域 X：")
    val locationY = createText(this, "通知區域 Y：")
    val width = createText(this, "通知區域寬度：")
    val height = createText(this, "通知區域高度：")
    val (borderLabel, borderButton) = createColorChooser(this, "邊框顏色：", borderColor, borderColor = _)
    val areaSelectionButton = createAreaSelectionButton()
    val (bgLabel, bgButton) = createColorChooser(this, "背景顏色：", bgColor, bgColor = _)
    val (fgLabel, fgButton) = createColorChooser(this, "文字顏色：", fontColor, fontColor = _)
    val (fontLabel, fontButton) = createFontChooser(this, "訊息字型：", messageFont = _)
    val (transparentLabel, transparentScale) = createScaleChooser(this, alphaTitle)
    val (displayTimeLabel, displayTimeSpinner) = createSpinner(this, "停留秒數：", 1, 120)
    val (fadeTimeLabel, fadeTimeSpinner) = createSpinner(this, "效果時間(ms)：", 1, 5000)
    val (spacingLabel, spacingSpinner) = createSpinner(this, "泡泡間距：", 1, 20)
    val previewButton = createPreviewButton()

    def createSpanLabel() = {
        val label = new Label(this, SWT.NONE)
        val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
        layoutData.horizontalSpan = 2
        label.setLayoutData(layoutData)
    }

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
                balloonController.addMessage("[%d] %s" format(count, message))
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
            displayTimeSpinner.getSelection * 1000, 
            fadeTimeSpinner.getSelection,
            spacingSpinner.getSelection
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

    def createAreaSelectionButton() =
    {
        val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
        val button = new Button(this, SWT.PUSH)
        layoutData.horizontalSpan = 2
        button.setLayoutData(layoutData)
        button.setText("選擇通知區域")
        button.addSelectionListener { e: SelectionEvent =>
            def oldArea = (locationX.getText.toInt, locationY.getText.toInt,
                           width.getText.toInt, height.getText.toInt)
            println("oldArea:" + oldArea)
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
            button.setText("停止預覽")
        }

        def stopPreview()
        {
            button.setText("開始預覽")
            balloonController.foreach{ controller =>
                testThread.foreach{_.setStop(true)}
                testThread = None
                controller.close()
            }
            balloonController = None
        }

        layoutData.horizontalSpan = 2
        button.setLayoutData(layoutData)
        button.setText("開始預覽")
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

    def setModifyListener() {
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
        displayTimeSpinner.setEnabled(isEnabled)
        fadeTimeSpinner.setEnabled(isEnabled)
        spacingSpinner.setEnabled(isEnabled)
        previewButton.setEnabled(isEnabled)
    }

    this.setLayout(gridLayout)
    this.setDefaultValue()
    this.setTextVerify()
    this.setModifyListener()
    this.tabItem.setText("泡泡通知")
    this.tabItem.setControl(this)
}

