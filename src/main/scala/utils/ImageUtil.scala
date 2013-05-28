package org.bone.ircballoon

import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.GC

import scala.util.control.Exception._

object ImageUtil
{
  import java.net.URL

  lazy val display = Display.getDefault

  def loadFromResource(path: String) = allCatch.opt {
    new Image(display, getClass().getResourceAsStream(path))
  }

  def loadFromFile(filePath: String) = allCatch.opt {
    new Image(display, filePath)
  }

  def loadFromURL(url: String) = allCatch.opt {
    val inputStream = new URL(url).openStream
    val image = new Image(Display.getDefault, inputStream)
    inputStream.close()
    image
  }

  def resize(image: Image, size: (Int, Int)) =
  {
    val (width, height) = size
    val scaled = new Image(display, width, height)
    val gc = new GC(scaled)

    gc.setAntialias(SWT.ON)
    gc.setInterpolation(SWT.HIGH)
    gc.drawImage(
      image, 0, 0,
      image.getBounds().width, image.getBounds().height,
      0, 0, width, height
    )

    gc.dispose()
    image.dispose()

    scaled
  }
}

