package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StackLayout

import org.eclipse.swt._

trait SWTHelper
{
    implicit def convertFromFont(font: Font): String = {
        val fontData = (font.getFontData)
        "%s / %d" format(fontData(0).getName, fontData(0).getHeight)
    }

    implicit def convertFromColor(color: Color): String = {
        "(%d, %d, %d)".format(color.getRed, color.getGreen, color.getBlue)
    }

    def createText(parent: Composite, title: String, style: Int = SWT.NONE) = 
    {
        val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
        val label = new Label(parent, SWT.LEFT)
        val text = new Text(parent, SWT.BORDER|style)

        label.setText(title)
        text.setLayoutData(layoutData)

        text
    }

}

class JustinSetting(parent: Composite) extends Composite(parent, SWT.NONE) with SWTHelper
{
    val gridLayout = new GridLayout(2,  false)
    val username = createText(this, "帳號：")
    val password = createText(this, "密碼：")

    this.setLayout(gridLayout)
}

class IRCSetting(parent: Composite) extends Composite(parent, SWT.NONE) with SWTHelper
{
    val gridLayout = new GridLayout(2,  false)
    val hostText = createText(this, "IRC 伺服器主機：")
    val portText = createText(this, "IRC 伺服器Port：")
    val password = createText(this, "IRC 伺服器密碼：", SWT.PASSWORD)
    val nickname = createText(this, "暱稱：")

    this.setLayout(gridLayout)
}

class BlockSetting(parent: Composite) extends Composite(parent, SWT.NONE) with SWTHelper
{
    var backgroundColor: Color = MyColor.Black
    var fontColor: Color = MyColor.White
    var messageFont: Font = Display.getDefault.getSystemFont

    val gridLayout = new GridLayout(4, false)
    val locationX = createText(this, "視窗位址 X：")
    val locationY = createText(this, "視窗位址 Y：")
    val width = createText(this, "視窗寬度：")
    val height = createText(this, "視窗高度：")
    val (bgLabel, bgButton) = createColorChooser("背景顏色：", backgroundColor = _)
    val (fgLabel, fgButton) = createColorChooser("文字顏色：", fontColor = _)
    val (fontLabel, fontButton) = createFontChooser("訊息字型：", messageFont = _)
    val (aa, bb) = createTransparentChooser("透明度：")

    def createTransparentChooser(title: String) =
    {
        val layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false)
        val layoutData2 = new GridData(SWT.FILL, SWT.CENTER, false, false)
        val label = new Label(this, SWT.LEFT)
        val slider = new Scale(this, SWT.HORIZONTAL)

        def updateLabel()
        {
            label.setText(title + slider.getSelection + "%")
        }

        label.setLayoutData(layoutData2)
        label.setText("透明度：")
        slider.setMaximum(100)
        slider.setMinimum(0)
        slider.setLayoutData(layoutData)
        slider.setSelection(20)
        slider.addSelectionListener(new SelectionAdapter() {
            override def widgetSelected(e: SelectionEvent) {
                updateLabel()
            }
        })

        updateLabel()

        (label, slider)
    }

    def createFontChooser(title: String, action: Font => Any) =
    {
        val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
        val label = new Label(this, SWT.LEFT)
        val button = new Button(this, SWT.PUSH)

        label.setText(title)
        button.setLayoutData(layoutData)
        button.setText(messageFont)
        button.addSelectionListener(new SelectionAdapter() {
            override def widgetSelected(e: SelectionEvent) {
                val fontDialog = new FontDialog(Main.shell)
                val fontData = fontDialog.open()

                if (fontData != null) {
                    val font = new Font(Display.getDefault, fontData)
                    action(font)
                    button.setText(font)
                }
            }
        })

        (label, button)
    }

    def createColorChooser(title: String, action: Color => Any) = {
        val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
        val label = new Label(this, SWT.LEFT)
        val button = new Button(this, SWT.PUSH)

        label.setText(title)
        button.setLayoutData(layoutData)
        button.setText(backgroundColor)
        button.addSelectionListener(new SelectionAdapter() {
            override def widgetSelected(e: SelectionEvent) {
                val colorDialog = new ColorDialog(Main.shell)
                val rgb = colorDialog.open()

                if (rgb != null) {
                    val color = new Color(Display.getDefault, rgb)
                    action(color)
                    button.setText(color)
                }
            }
        })

        (label, button)
    }

    this.setLayout(gridLayout)
}

class BalloonSetting(parent: Composite) extends Composite(parent, SWT.NONE) with SWTHelper
{
    val gridLayout = new GridLayout(4, false)
    val locationX = createText(this, "視窗位址 X：")
    val locationY = createText(this, "視窗位址 Y：")

    this.setLayout(gridLayout)

}


object Main
{
    val display = new Display
    val shell = new Shell(display)
    var notification: NotificationBlock = null

    val stackLayout = new StackLayout
    val displayStackLayout = new StackLayout

    val logginType = createLogginType()
    val settingGroup = createSettingGroup()
    val ircButton = createIRCButton()
    val justinButton = createJustinButton()
    val settingPages = createSettingPages()
    val ircSetting = new IRCSetting(settingPages)
    val justinSetting = new JustinSetting(settingPages)

    val displayType = createDisplayType()
    val displayGroup = createDisplayGroup()
    val blockButton = createBlockButton()
    val balloonButton = createBalloonButton()

    val displayPages = createDisplayPages()
    val blockSetting = new BlockSetting(displayPages)
    val balloonSetting = new BalloonSetting(displayPages)

    def switchDisplayPages()
    {
        (blockButton.getSelection, balloonButton.getSelection) match {
            case (true, _) => displayStackLayout.topControl = blockSetting
            case (_, true) => displayStackLayout.topControl = balloonSetting
        }

        displayPages.layout()
    }

    def createDisplayPages() =
    {
        val composite = new Composite(shell, SWT.NONE)
        val spanLayout = new GridData(SWT.FILL, SWT.NONE, true, false)
        spanLayout.horizontalSpan = 2
        composite.setLayoutData(spanLayout)
        composite.setLayout(displayStackLayout)
        composite
    }

    def createSettingGroup() =
    {
        val group = new Group(shell, SWT.SHADOW_NONE)
        val spanLayout = new GridData(SWT.FILL, SWT.NONE, true, false)
        group.setLayoutData(spanLayout)
        group.setLayout(new RowLayout)
        group
    }


    def createDisplayGroup() =
    {
        val group = new Group(shell, SWT.SHADOW_NONE)
        val spanLayout = new GridData(SWT.FILL, SWT.NONE, true, false)
        group.setLayoutData(spanLayout)
        group.setLayout(new RowLayout)
        group
    }

    def createDisplayType() = 
    {
        val label = new Label(shell, SWT.LEFT)
        label.setText("顯示方式：")
        label
    }

    def createBlockButton() = 
    {
        val button = new Button(displayGroup, SWT.RADIO)
        button.setText("固定區塊")
        button.setSelection(true)
        button.addSelectionListener(new SelectionAdapter() {
            override def widgetSelected(e: SelectionEvent) {
                switchDisplayPages()
            }
        })
        button
    }

    def createBalloonButton() = 
    {
        val button = new Button(displayGroup, SWT.RADIO)
        button.setText("泡泡通知")
        button.addSelectionListener(new SelectionAdapter() {
            override def widgetSelected(e: SelectionEvent) {
                switchDisplayPages()
            }
        })
        button
    }

    def switchSettingPages()
    {
        (ircButton.getSelection, justinButton.getSelection) match {
            case (true, _) => stackLayout.topControl = ircSetting
            case (_, true) => stackLayout.topControl = justinSetting
        }

        settingPages.layout()
    }

    def createSettingPages() = 
    {
        val composite = new Composite(shell, SWT.NONE)
        val spanLayout = new GridData(SWT.FILL, SWT.NONE, true, false)
        spanLayout.horizontalSpan = 2
        composite.setLayoutData(spanLayout)
        composite.setLayout(stackLayout)
        composite
    }

    def createLogginType() = 
    {
        val label = new Label(shell, SWT.LEFT|SWT.BORDER)
        label.setText("設定方式：")
        label
    }

    def createIRCButton() =
    {
        val button = new Button(settingGroup, SWT.RADIO)
        button.setText("IRC")
        button.setSelection(true)
        button.addSelectionListener(new SelectionAdapter() {
            override def widgetSelected(e: SelectionEvent) {
                switchSettingPages()
            }
        })
        button
    }
    
    def createJustinButton() = 
    {
        val button = new Button(settingGroup, SWT.RADIO)
        button.setText("Justin / Twitch")
        button.addSelectionListener(new SelectionAdapter() {
            override def widgetSelected(e: SelectionEvent) {
                switchSettingPages()
            }
        })
        button
    }

    def setupLayout()
    {
        val gridLayout = new GridLayout(2,  false)
        shell.setLayout(gridLayout)
    }

    def main(args: Array[String])
    {
        setupLayout()
        switchSettingPages()
        switchDisplayPages()

        shell.setText("IRC 聊天通知")
        shell.pack()
        shell.open()

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose()
        sys.exit()
    }
}
