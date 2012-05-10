package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StackLayout

import org.eclipse.swt._

object TestWindow extends SWTHelper
{
    val display = new Display
    val shell = new Shell(display)
    val canvas = new Canvas(shell, SWT.NONE)

    var locationX: Int = 0
    var locationY: Int = 0
    var height: Int = 0
    var width: Int = 0

    def setAreaSelection()
    {
        canvas.addPaintListener(new PaintListener() {
            override def paintControl(e: PaintEvent) {
                e.gc.setBackground(MyColor.Blue)
                e.gc.fillRectangle(locationX, locationY, width, height)
                e.gc.setForeground(MyColor.White)
                e.gc.setLineWidth(3)
                e.gc.drawRectangle(locationX, locationY, width, height)
            }
        })

        canvas.addMouseMoveListener(new MouseMoveListener() {
            override def mouseMove(e: MouseEvent) {
                if ((e.stateMask & SWT.BUTTON1) != 0) {
                    height = e.y - locationY
                    width = e.x - locationX
                    println("Draw:(%d, %d, %d, %d):" + locationX, locationY, width, height)
                    canvas.redraw()
                }
            }
        })

        canvas.addMouseListener(new MouseAdapter() {
            override def mouseDoubleClick(e: MouseEvent) {
                shell.dispose()
            }

            override def mouseDown(e: MouseEvent) {
                println("Mouse down:" + e)
                locationX = e.x
                locationY = e.y
                height = 0
                width = 0
            }
        })

    }

    def open(): (Int, Int, Int, Int) =
    {
        shell.setLayout(new FillLayout())
        shell.open()
        shell.setMaximized(true)
        shell.setFullScreen(true)
        shell.setBackgroundMode(SWT.INHERIT_DEFAULT)
        shell.setBackground(MyColor.Black)
        shell.setAlpha(150)

        setAreaSelection()

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }

        display.dispose()
        (locationX, locationY, width, height)
    }

    def main(args: Array[String])
    {
        println(open())
    }
}
