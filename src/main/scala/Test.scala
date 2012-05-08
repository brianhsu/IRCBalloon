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
    implicit def convertToSelectionaAdapter(action: SelectionEvent => Any) = {
        new SelectionAdapter() {
            override def widgetSelected(e: SelectionEvent) {
                action(e)
            }
        }
    }

    implicit def convertFromFont(font: Font): String = {
        val fontData = (font.getFontData)
        "%s / %d" format(fontData(0).getName, fontData(0).getHeight)
    }

    implicit def convertFromColor(color: Color): String = {
        def paddingHex(x: Int) = x.toHexString match {
            case hex if hex.length >= 2 => hex
            case hex if hex.length <= 1 => "0" + hex
        }

        "#%s%s%s".format(paddingHex(color.getRed), 
                         paddingHex(color.getGreen), 
                         paddingHex(color.getBlue)).toUpperCase
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

    def createScaleChooser(parent: Composite, title: String) =
    {
        val layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false)
        val layoutData2 = new GridData(SWT.FILL, SWT.CENTER, false, false)
        val label = new Label(parent, SWT.LEFT)
        val scale = new Scale(parent, SWT.HORIZONTAL)

        def updateLabel()
        {
            label.setText(title + scale.getSelection + "%")
        }

        label.setLayoutData(layoutData2)
        label.setText(title)
        scale.setMaximum(100)
        scale.setMinimum(0)
        scale.setLayoutData(layoutData)
        scale.setSelection(20)
        scale.addSelectionListener { e: SelectionEvent =>
            updateLabel()
        }

        updateLabel()

        (label, scale)
    }

    def createFontChooser(parent: Composite, title: String, action: Font => Any) =
    {
        val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
        val label = new Label(parent, SWT.LEFT)
        val button = new Button(parent, SWT.PUSH)

        label.setText(title)
        button.setLayoutData(layoutData)
        button.setText(Display.getDefault.getSystemFont)
        button.addSelectionListener { e: SelectionEvent =>
            val fontDialog = new FontDialog(Main.shell)
            val fontData = fontDialog.open()

            if (fontData != null) {
                val font = new Font(Display.getDefault, fontData)
                action(font)
                button.setText(font)
            }
        }

        (label, button)
    }

    def createColorChooser(parent: Composite, title: String, 
                           defaultColor: Color, action: Color => Any) = 
    {
        val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
        val label = new Label(parent, SWT.LEFT)
        val button = new Button(parent, SWT.PUSH)

        label.setText(title)
        button.setLayoutData(layoutData)
        button.setText(defaultColor)
        button.addSelectionListener{ e: SelectionEvent =>
            val colorDialog = new ColorDialog(Main.shell)
            val rgb = colorDialog.open()

            if (rgb != null) {
                val color = new Color(Display.getDefault, rgb)
                action(color)
                button.setText(color)
            }
        }

        (label, button)
    }

    def createSpinner(parent: Composite, title: String, min: Int, max:Int) =
    {
        val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
        val label = new Label(parent, SWT.LEFT)
        val spinner = new Spinner(parent, SWT.NONE)

        label.setText(title)
        spinner.setLayoutData(layoutData)
        spinner.setMaximum(max)
        spinner.setMinimum(min)

        (label, spinner)
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


    this.setLayout(gridLayout)
}

class BalloonSetting(parent: Composite) extends Composite(parent, SWT.NONE) with SWTHelper
{
    val gridLayout = new GridLayout(4, false)
    val locationX = createText(this, "視窗位址 X：")
    val locationY = createText(this, "視窗位址 Y：")

    this.setLayout(gridLayout)

}


object Main extends SWTHelper
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
        button.addSelectionListener { e:SelectionEvent =>
            switchDisplayPages()
        }
        button
    }

    def createBalloonButton() = 
    {
        val button = new Button(displayGroup, SWT.RADIO)
        button.setText("泡泡通知")
        button.addSelectionListener { e: SelectionEvent =>
            switchDisplayPages()
        }
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
        button.addSelectionListener{ e: SelectionEvent =>
            switchSettingPages()
        }
        button
    }
    
    def createJustinButton() = 
    {
        val button = new Button(settingGroup, SWT.RADIO)
        button.setText("Justin / Twitch")
        button.addSelectionListener { e: SelectionEvent =>
            switchSettingPages()
        }
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
