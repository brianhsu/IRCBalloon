package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StackLayout

import org.eclipse.swt._

class BlockSetting(parent: TabFolder, onModify: ModifyEvent => Any) extends 
      Composite(parent, SWT.NONE) with SWTHelper
{
    var blockBackgroundImage: Option[String] = None

    val tabItem = new TabItem(parent, SWT.NONE)

    var bgColor: Color = MyColor.Black
    var fontColor: Color = MyColor.White
    var borderColor: Color = MyColor.White
    var messageFont: Font = Display.getDefault.getSystemFont

    val alphaTitle = "透明度："

    val gridLayout = new GridLayout(4, false)
    val locationX = createText(this, "視窗位址 X：")
    val locationY = createText(this, "視窗位址 Y：")
    val width = createText(this, "視窗寬度：")
    val height = createText(this, "視窗高度：")
    val (borderLabel, borderButton) = createColorChooser(this, "邊框顏色：", borderColor, borderColor = _)
    val span = createSpanLabel()
    val (bgImageLabel, bgImageButton, bgImageCheck) = createBackgroundImage()
    val (bgLabel, bgButton) = createColorChooser(this, "背景顏色：", bgColor, bgColor = _)
    val (fgLabel, fgButton) = createColorChooser(this, "文字顏色：", fontColor, fontColor = _)
    val (fontLabel, fontButton) = createFontChooser(this, "訊息字型：", messageFont = _)
    val (transparentLabel, transparentScale) = createScaleChooser(this, alphaTitle)
    val (messageSizeLabel, messageSizeSpinner) = createSpinner(this, "訊息數量：", 1, 50)
    val previewButton = createPreviewButton()
    val noticeLabel = createNoticeLabel()

    def setBlockBackgroundImage(imageFile: String) 
    {
        bgImageButton.setToolTipText(imageFile)
        bgImageButton.setText(imageFile)
        blockBackgroundImage = Some(imageFile)        
    }

    def createBackgroundImage() = {
        val layoutData = new GridData(SWT.FILL, SWT.FILL, true, false)
        val layoutData2 = new GridData(SWT.FILL, SWT.FILL, true, false)
        layoutData2.horizontalSpan = 2

        val label = new Label(this, SWT.LEFT)
        val button = new Button(this, SWT.PUSH)
        val clear = new Button(this, SWT.PUSH)

        label.setText("背景圖案：")
        clear.setLayoutData(layoutData2)
        clear.setText("移除背景圖案")
        clear.addSelectionListener { e: SelectionEvent =>
            button.setText("瀏覽……")
            blockBackgroundImage = None
        }

        button.setLayoutData(layoutData)
        button.setText("瀏覽……")
        button.addSelectionListener { e: SelectionEvent =>
            val fileDialog = new FileDialog(MainWindow.shell, SWT.OPEN)
            val imageFile = fileDialog.open()

            if (imageFile != null) {
                setBlockBackgroundImage(imageFile)
            }
        }

        (label, button, clear)
    }

    def createSpanLabel() = {
        val layoutData = new GridData(SWT.LEFT, SWT.CENTER, true, false)
        val label = new Label(this, SWT.LEFT|SWT.WRAP)
        layoutData.horizontalSpan =2
        label.setLayoutData(layoutData)
        label
    }

    def createNoticeLabel() =
    {
        val layoutData = new GridData(SWT.LEFT, SWT.CENTER, true, false)
        val label = new Label(this, SWT.LEFT|SWT.WRAP)
        layoutData.horizontalSpan =4
        label.setLayoutData(layoutData)
        label.setText("註：可以直接用滑鼠拖拉聊天室窗，並且用視窗右下角來縮放。")
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
                notificationBlock.addMessage(SystemMessage("[%d] %s" format(count, message)))
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
            fontColor, messageFont, messageSize, blockBackgroundImage
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

    def updatePreviewButtonState(e: ModifyEvent)
    {
        previewButton.setEnabled(isSettingOK)
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
        messageSizeSpinner.setEnabled(isEnabled)
        previewButton.setEnabled(isEnabled)
    }

    this.setLayout(gridLayout)
    this.setDefaultValue()
    this.setTextVerify()
    this.setModifyListener()
    this.tabItem.setText("固定區塊")
    this.tabItem.setControl(this)
}

