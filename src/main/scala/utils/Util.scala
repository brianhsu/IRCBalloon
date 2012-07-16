package org.bone.ircballoon

import org.eclipse.swt._
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.graphics._

import org.xnap.commons.i18n.I18nFactory

import java.util.Locale
import scala.util.control.Exception._

object I18N
{
    val flags = I18nFactory.FALLBACK|I18nFactory.READ_PROPERTIES
    val i18n = I18nFactory.getI18n(getClass(), Locale.getDefault, flags)
}

object MyIcon
{
    import ImageUtil.loadFromResource

    val appIcon = loadFromResource("/appIcon.png").get
    val ircOP = loadFromResource("/opIcon.png").get
    val preference = loadFromResource("/preference.png").get

    val close = loadFromResource("/close.png").get
    val add = loadFromResource("/add.png").get
    val remove = loadFromResource("/remove.png").get
}

object MyColor
{
    lazy val Black = new Color(Display.getDefault, 0, 0, 0)
    lazy val White = new Color(Display.getDefault, 255, 255, 255)
    lazy val Blue = new Color(Display.getDefault, 100, 100, 255)
}

object MyFont
{
    lazy val DefaultFont = Display.getDefault.getSystemFont
    lazy val DefaultFontName = DefaultFont.getFontData()(0).getName
    lazy val DefaultFontSize = DefaultFont.getFontData()(0).getHeight
    lazy val DefaultFontStyle = DefaultFont.getFontData()(0).getStyle

    lazy val LargeFont = new Font(
        Display.getDefault, DefaultFontName, 
        DefaultFontSize + 3, 
        SWT.BOLD
    )
}

object MessageSample
{
    import scala.util.Random

    val samples = List(
        "guest: 這是第一個測試", 
        "user: 哈囉，大家好，我是 user",
        "guest: This is a test.",
        "long: 這是非常非常非常非常長的一段文字，一二三四五六七八九十，甲乙丙丁戊己庚辛",
        "tester: Another test.",
        "beta: This is a beta test"
    )

    def random(size: Int) = {
        val repeat = (size / samples.length) + 1
        Random.shuffle(List.fill(repeat)(samples).flatten).take(size)
    }
}

