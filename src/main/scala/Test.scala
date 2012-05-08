package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StackLayout

import org.eclipse.swt._

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
