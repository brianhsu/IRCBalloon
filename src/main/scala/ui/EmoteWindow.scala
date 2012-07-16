package org.bone.ircballoon

import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.custom.StackLayout
import org.eclipse.swt.custom.ScrolledComposite

import org.eclipse.swt._
import I18N.i18n._
import ImageUtil._

class AddEmoteDialog(parent: Shell) extends Dialog(parent, SWT.APPLICATION_MODAL) 
                                    with SWTHelper
{
    var result: Option[(String, String)] = None

    val display = parent.getDisplay()
    val shell = new Shell(parent, SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL)
    val textTarget = createTextTarget()
    val emoteIcon = createIconField()
    val (okButton, cancelButton) = createButtons()

    def setOKButtonState() =
    {
        (textTarget.getText.trim.length > 0 && emoteIcon.getText.trim.length > 0) match {
            case true => okButton.setEnabled(true)
            case false => okButton.setEnabled(false)
        }
    }

    def setupListener()
    {
        cancelButton.addSelectionListener { e: SelectionEvent => shell.dispose() }
        textTarget.addModifyListener { e: ModifyEvent => setOKButtonState() }
        emoteIcon.addModifyListener { e: ModifyEvent => setOKButtonState() }
        okButton.addSelectionListener { e: SelectionEvent =>
            result = Some((textTarget.getText.trim, emoteIcon.getText.trim))
            shell.dispose()
        }
    }

    def createTextTarget() =
    {
        val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
        val label = new Label(shell, SWT.LEFT)
        val text = new Text(shell, SWT.BORDER)

        layoutData.horizontalSpan = 2

        label.setText(tr("Target text:"))
        text.setLayoutData(layoutData)

        text
    }

    def createIconField() =
    {
        val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
        val label = new Label(shell, SWT.LEFT)
        val text = new Text(shell, SWT.BORDER)
        val browse = new Button(shell, SWT.PUSH)

        label.setText(tr("Emote Icon:"))
        text.setLayoutData(layoutData)
        browse.setText(tr("Browse..."))
        browse.addSelectionListener { e: SelectionEvent =>

            val extensions = Array(
                "*.png;*.jpg;*.jpeg;*.gif;",
                "*.PNG;*.JPG;*.JPEG;*.GIF"
            )

            val fileDialog = new FileDialog(shell, SWT.OPEN)
            fileDialog.setFilterExtensions(extensions)
            
            val path = fileDialog.open()

            if (path != null && path != "") {
                text.setText(path)
            }
        }

        text
    }

    def createButtons() =
    {
        val layout = new GridLayout(2, true)
        val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
        val composite = new Composite(shell, SWT.NONE)

        layoutData.horizontalSpan = 3

        composite.setLayout(layout)
        composite.setLayoutData(layoutData)

        val okButton = new Button(composite, SWT.PUSH)
        val cancelButton = new Button(composite, SWT.PUSH)

        val layoutData2 = new GridData(SWT.FILL, SWT.NONE, true, false)
        okButton.setLayoutData(layoutData2)
        okButton.setText(tr("OK"))
        okButton.setEnabled(false)
        cancelButton.setLayoutData(layoutData2)
        cancelButton.setText(tr("Cancel"))

        (okButton, cancelButton)
    }

    def open() =
    {
        val gridLayout = new GridLayout(3, false)

        setupListener()
        shell.setLayout(gridLayout)
        shell.setText(tr("Add Emote Icon"))
        shell.pack()
        shell.open()

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep()
        }

        result
    }

}

class EmoteWindow(parent: Shell) extends SWTHelper
{
    val shell = new Shell(parent, SWT.APPLICATION_MODAL|SWT.DIALOG_TRIM)
    val gridLayout = new GridLayout(2, false)
    val defaultEmotesCheckBox = createCheckBox(shell, tr("Use default emote icons"))
    val emoteTable = createTable()
    val toolBar = createToolBar()
    val closeButton = createCloseButton()

    def setListener()
    {
        defaultEmotesCheckBox.addSelectionListener { e: SelectionEvent =>
            Preference.usingDefaultEmotes = defaultEmotesCheckBox.getSelection
        }
    }

    def createTable() =
    {
        val layoutData = new GridData(SWT.FILL, SWT.FILL, true, true)
        val table = new Table(shell, SWT.FULL_SELECTION|SWT.BORDER)
        val columns = List("Text", "Image").map { title =>
            val column = new TableColumn(table, SWT.NONE)
            column.setText(title)
            column
        }

        IRCEmotes.getCustomEmotes.foreach { case(text, emoteIcon) =>
            val tableItem = new TableItem(table, SWT.NONE)
            tableItem.setText(Array(text, emoteIcon.file))
        }

        table.setLinesVisible(true)
        table.setHeaderVisible(true)
        table.setLayoutData(layoutData)
        table.getColumns.foreach(_.pack())

        table
    }

    def createToolBar() = 
    {
        val layoutData = new GridData(SWT.FILL, SWT.FILL, false, false)
        val toolBar = new ToolBar(shell, SWT.VERTICAL|SWT.RIGHT)

        val addButton = new ToolItem(toolBar, SWT.PUSH)
        val removeButton = new ToolItem(toolBar, SWT.PUSH)
        
        addButton.setImage(MyIcon.add)
        addButton.setText(tr("Add"))
        addButton.addSelectionListener { e: SelectionEvent =>
            val dialog = new AddEmoteDialog(shell)

            dialog.open().foreach { case(text, imageFile) =>
                
                loadFromFile(imageFile).foreach { image =>

                    val tableItem = new TableItem(emoteTable, SWT.NONE)
                    tableItem.setText(Array(text, imageFile))

                    IRCEmotes.addEmote(text, imageFile)
                }
            }
        }

        removeButton.setImage(MyIcon.remove)
        removeButton.setText(tr("Remove"))
        removeButton.addSelectionListener { e: SelectionEvent =>
            for (index <- emoteTable.getSelectionIndices) {
                val targetText = emoteTable.getItem(index).getText(0)
                emoteTable.remove(index)
                IRCEmotes.removeEmote(targetText)
            }
        }

        toolBar.setLayoutData(layoutData)
        toolBar
    }

    def createCloseButton() =
    {
        val layoutData = new GridData(SWT.RIGHT, SWT.FILL, false, false)
        val button = new Button(shell, SWT.PUSH)

        layoutData.horizontalSpan = 2

        button.setLayoutData(layoutData)
        button.setText(tr("Close"))
        button.setImage(MyIcon.close)
        button.addSelectionListener { e: SelectionEvent =>
            shell.dispose()
        }
        button
    }

    def open()
    {
        defaultEmotesCheckBox.setSelection(Preference.usingDefaultEmotes)

        setListener()

        shell.setLayout(gridLayout)
        shell.setText("Emote Preference")
        shell.setSize(600, 400)
        shell.open()
    }

}
