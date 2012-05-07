package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StackLayout

import org.eclipse.swt._

class JustinSetting(parent: Composite) extends Composite(parent, SWT.NONE)
{
    this.setLayout(new FillLayout)
    val button = new Button(this, SWT.PUSH)
    button.setText("Hello Justin")
}

class IRCSetting(parent: Composite) extends Composite(parent, SWT.NONE)
{
    this.setLayout(new FillLayout)
    val button = new Button(this, SWT.PUSH)
    button.setText("Hello IRC")
}

object Main
{
    val display = new Display
    val shell = new Shell(display)
    var notification: NotificationBlock = null

    val stackLayout = new StackLayout

    val logginType = createLogginType()
    val ircButton = createIRCButton()
    val justinButton = createJustinButton()
    val settingPages = createSettingPages()
    val ircSetting = new IRCSetting(settingPages)
    val justinSetting = new JustinSetting(settingPages)

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
        val spanLayout = new GridData(SWT.FILL, SWT.FILL, true, true)
        spanLayout.horizontalSpan = 3
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
        val button = new Button(shell, SWT.RADIO)
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
        val button = new Button(shell, SWT.RADIO)
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
        val gridLayout = new GridLayout(3,  false)
        shell.setLayout(gridLayout)
    }

    def main(args: Array[String])
    {
        setupLayout()
        switchSettingPages()

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
