package org.bone.ircballoon

import org.bone.ircballoon.model._
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

class AddAvatarDialog(parent: Shell) extends Dialog(parent, SWT.APPLICATION_MODAL) 
                                     with SWTHelper
{
  var result: Option[(String, String)] = None

  val display = parent.getDisplay()
  val shell = new Shell(parent, SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL)

  val nickname = createTextTarget()
  val avatar = createIconField()
  val (okButton, cancelButton) = createButtons()

  def setOKButtonState() =
  {
    (nickname.getText.trim.length > 0 && avatar.getText.trim.length > 0) match {
      case true => okButton.setEnabled(true)
      case false => okButton.setEnabled(false)
    }
  }

  def setupListener()
  {
    cancelButton.addSelectionListener { e: SelectionEvent => shell.dispose() }
    nickname.addModifyListener { e: ModifyEvent => setOKButtonState() }
    avatar.addModifyListener { e: ModifyEvent => setOKButtonState() }
    okButton.addSelectionListener { e: SelectionEvent =>
      result = Some(nickname.getText.trim, avatar.getText.trim)
      shell.dispose();
    }
  }

  def createTextTarget() =
  {
    val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
    val label = new Label(shell, SWT.LEFT)
    val text = new Text(shell, SWT.BORDER)

    layoutData.horizontalSpan = 2

    label.setText(tr("Nickname:"))
    text.setLayoutData(layoutData)

    text
  }

  def createIconField() =
  {
    val layoutData = new GridData(SWT.FILL, SWT.NONE, true, false)
    val label = new Label(shell, SWT.LEFT)
    val text = new Text(shell, SWT.BORDER)
    val browse = new Button(shell, SWT.PUSH)

    label.setText(tr("Avatar:"))
    text.setLayoutData(layoutData)
    browse.setText(tr("Browse..."))
    browse.addSelectionListener { e: SelectionEvent =>

      val extensions = Array(
        "*.png;*.jpg;*.jpeg;*.gif;" + 
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
    shell.setText(tr("Add Avatar"))
    shell.pack()
    shell.open()

    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) display.sleep()
    }

    result
  }

}


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
      Preference.displayAvatar = displayAvatar.getSelection
    }

    onlyAvatar.addSelectionListener { e:SelectionEvent => 
      Preference.onlyAvatar = onlyAvatar.getSelection
    }

    usingTwitchAvatar.addSelectionListener { e:SelectionEvent => 
      Preference.usingTwitchAvatar = usingTwitchAvatar.getSelection
    }
  }

  def createTable() =
  {
    val layoutData = new GridData(SWT.FILL, SWT.FILL, true, true)
    val table = new Table(shell, SWT.FULL_SELECTION|SWT.BORDER)
    val columns = List(tr("Nickname"), tr("Avatar")).map { title =>
      val column = new TableColumn(table, SWT.NONE)
      column.setText(title)
      column
    }

    table.setLinesVisible(true)
    table.setHeaderVisible(true)
    table.setLayoutData(layoutData)

    TwitchUser.getAvatars.foreach { case(nickname, avatar) =>
      val tableItem = new TableItem(table, SWT.NONE)
      tableItem.setText(Array(nickname, avatar.file))
    }

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
      val avatarDialog = new AddAvatarDialog(shell)
        
      avatarDialog.open().foreach { case(nickname, avatarPath) =>
        try {

          val image = loadFromFile(avatarPath).get
          val tableItem = new TableItem(avatarTable, SWT.NONE)

          tableItem.setText(Array(nickname, avatarPath))
          TwitchUser.addAvatar(nickname, avatarPath)
                
        } catch {
          case e: Exception =>
        }
      }
    }

    removeButton.setImage(MyIcon.remove)
    removeButton.setText(tr("Remove"))
    removeButton.addSelectionListener { e: SelectionEvent =>
      for (index <- avatarTable.getSelectionIndices) {
        val nickname = avatarTable.getItem(index).getText(0)
        avatarTable.remove(index)
        TwitchUser.removeAvatar(nickname)
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

    displayAvatar.setSelection(Preference.displayAvatar)
    usingTwitchAvatar.setSelection(Preference.usingTwitchAvatar)
    onlyAvatar.setSelection(Preference.onlyAvatar)

    shell.setLayout(gridLayout)
    shell.setText(tr("Avatar / Nickname Preference"))
    shell.setSize(600, 400)
    shell.open()
  }

}
