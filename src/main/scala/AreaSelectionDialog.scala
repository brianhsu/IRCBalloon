package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StackLayout

import org.eclipse.swt._
import I18N.i18n._

object AreaSelectionDialog
{
    def doNothing(x: Int, y: Int, width: Int, height: Int) {}
}

class AreaSelectionDialog(default: (Int, Int, Int, Int) = (0, 0, 0, 0),
                          onChange: (Int, Int, Int, Int) => Any = 
                          AreaSelectionDialog.doNothing _) extends SWTHelper
{
    val display = MainWindow.display
    val shell = new Shell(MainWindow.shell, SWT.ON_TOP|SWT.NO_TRIM)
    val canvas = new Canvas(shell, SWT.NONE)

    var locationX: Int = default._1
    var locationY: Int = default._2
    var width: Int = default._3
    var height: Int = default._4

    def setAreaSelection()
    {
        canvas.addPaintListener(new PaintListener() {
            override def paintControl(e: PaintEvent) {
                e.gc.setForeground(MyColor.White)
                e.gc.setBackground(MyColor.Blue)
                e.gc.fillRectangle(locationX, locationY, width, height)
                e.gc.setLineWidth(3)
                e.gc.drawRectangle(locationX, locationY, width, height)
                e.gc.setFont(MyFont.LargeFont)
                e.gc.drawText(
                    tr("Drag notification area and double click to confirm"), 
                    shell.getSize.x / 2 - 200, 
                    shell.getSize.y / 2,
                    true
                )

            }
        })

        canvas.addMouseMoveListener(new MouseMoveListener() {
            override def mouseMove(e: MouseEvent) {
                if ((e.stateMask & SWT.BUTTON1) != 0) {
                    height = e.y - locationY
                    width = e.x - locationX
                    canvas.redraw()
                    onChange(locationX, locationY, width, height)
                }
            }
        })

        canvas.addMouseListener(new MouseAdapter() {
            override def mouseDoubleClick(e: MouseEvent) {
                shell.dispose()
            }

            override def mouseDown(e: MouseEvent) {
                locationX = e.x
                locationY = e.y
                height = 0
                width = 0
            }
        })

    }

    def open(): (Int, Int, Int, Int) =
    {
        val width = display.getClientArea.width
        val height = display.getClientArea.height
        shell.setLayout(new FillLayout())
        shell.open()
        shell.setLocation(0, 0)
        shell.setSize(width, height)
        shell.setBackgroundMode(SWT.INHERIT_DEFAULT)
        shell.setBackground(MyColor.Black)
        shell.setAlpha(150)

        setAreaSelection()

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }

        (locationX, locationY, width, height)
    }
}
