package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText

import org.eclipse.swt._


object Main
{
    val display = new Display
    val shell = new Shell(display)
    var notification: NotificationBlock = null

    def initWidget() =
    {
        val button1 = new Button(shell, SWT.PUSH)
        button1.setText("開視窗")
        button1.addSelectionListener(new SelectionAdapter() {
            override def widgetSelected(e: SelectionEvent) {
                println("Clicked")
                Main.this.notification = new NotificationBlock
                Main.this.notification.open()
            }
        })

        val button2 = new Button(shell, SWT.PUSH)
        button2.setText("關視窗")
        button2.addSelectionListener(new SelectionAdapter() {
            override def widgetSelected(e: SelectionEvent) {
                println("Close Clicked")
                Main.this.notification.close()
            }
        })

    }

    def main(args: Array[String])
    {
        initWidget()
        shell.setText("Testing")
        shell.setLayout(new RowLayout)
        shell.pack()
        shell.open()

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose()
        sys.exit()
    }
}
