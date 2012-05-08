package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StackLayout

import org.eclipse.swt._

class BlockSetting(parent: Composite, onModify: ModifyEvent => Any) extends 
      Composite(parent, SWT.NONE) with SWTHelper
{
    var bgColor: Color = MyColor.Black
    var fgColor: Color = MyColor.White
    var messageFont: Font = Display.getDefault.getSystemFont

    val gridLayout = new GridLayout(4, false)
    val locationX = createText(this, "視窗位址 X：")
    val locationY = createText(this, "視窗位址 Y：")
    val width = createText(this, "視窗寬度：")
    val height = createText(this, "視窗高度：")
    val (bgLabel, bgButton) = createColorChooser(this, "背景顏色：", bgColor, bgColor = _)
    val (fgLabel, fgButton) = createColorChooser(this, "文字顏色：", fgColor, fgColor = _)
    val (fontLabel, fontButton) = createFontChooser(this, "訊息字型：", messageFont = _)
    val (transparentLabel, transparentScale) = createScaleChooser(this, "透明度：")
    val (messageSizeLabel, messageSizeSpinner) = createSpinner(this, "訊息數量：", 1, 50)
    val previewButton = createPreviewButton()

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
                notificationBlock.addMessage("[%d] %s" format(count, message))
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
            MyColor.White, bgColor, alpha, 
            fgColor, messageFont, messageSize
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
            button.setText("停止預覽")
        }

        def stopPreview()
        {
            button.setText("開始預覽")
            notificationBlock.foreach{ block =>
                testThread.foreach{_.setStop(true)}
                testThread = None
                block.close()
            }
            notificationBlock = None
        }

        layoutData.horizontalSpan = 2
        button.setLayoutData(layoutData)
        button.setText("開始預覽")
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

    def setModifyListener() {
        locationX.addModifyListener(onModify)
        locationY.addModifyListener(onModify)
        width.addModifyListener(onModify)
        height.addModifyListener(onModify)
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
    }

    this.setLayout(gridLayout)
    this.setDefaultValue()
    this.setTextVerify()
    this.setModifyListener()
}

