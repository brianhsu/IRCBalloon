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

class AvatarWindow(parent: Shell) extends SWTHelper
{
    val shell = new Shell(parent, SWT.APPLICATION_MODAL|SWT.DIALOG_TRIM)
    val gridLayout = new GridLayout(2, false)

    val displayAvatar = createCheckBox(shell, tr("Display avatar"))
    val usingTwitchAvatar = createCheckBox(shell, tr("Using avatar from Justin.TV / Twitch"))
    val onlyAvatar = createCheckBox(shell, tr("Don't display nickname when user has Avatar"))

    val avatarTable = createTable()
    val toolBar = createToolBar()

    val closeButton = createCloseButton()

    def setListener()
    {
        displayAvatar.addSelectionListener { e: SelectionEvent =>
            Avatar.displayAvatar = displayAvatar.getSelection
        }

        onlyAvatar.addSelectionListener { e:SelectionEvent => 
            Avatar.onlyAvatar = onlyAvatar.getSelection
        }

        usingTwitchAvatar.addSelectionListener { e:SelectionEvent => 
            Avatar.usingTwitchAvatar = usingTwitchAvatar.getSelection
        }
    }

    def createTable() =
    {
        val layoutData = new GridData(SWT.FILL, SWT.FILL, true, true)
        val table = new Table(shell, SWT.FULL_SELECTION|SWT.BORDER)
        val columns = List("Nickname", "Avatar").map { title =>
            val column = new TableColumn(table, SWT.NONE)
            column.setText(title)
            column
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
        }

        removeButton.setImage(MyIcon.remove)
        removeButton.setText(tr("Remove"))
        removeButton.addSelectionListener { e: SelectionEvent =>
            for (index <- avatarTable.getSelectionIndices) {
                val targetText = avatarTable.getItem(index).getText(0)
                avatarTable.remove(index)
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
        setListener()

        displayAvatar.setSelection(Avatar.displayAvatar)
        usingTwitchAvatar.setSelection(Avatar.usingTwitchAvatar)
        onlyAvatar.setSelection(Avatar.onlyAvatar)

        shell.setLayout(gridLayout)
        shell.setText("Avatar / Nickname Preference")
        shell.setSize(600, 400)
        shell.open()
    }

}
