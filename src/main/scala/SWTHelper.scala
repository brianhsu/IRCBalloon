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
    implicit def convertToVerifyAdapter(action: VerifyEvent => Any) = {
        new VerifyListener() {
            override def verifyText(e: VerifyEvent) {
                action(e)
            }
        }
    }
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

