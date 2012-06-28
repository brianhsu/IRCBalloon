package org.bone.ircballoon

import org.eclipse.swt._
import org.eclipse.swt.widgets.{List => SWTList, _}
import org.eclipse.swt.layout._
import org.eclipse.swt.events._
import org.eclipse.swt.graphics._
import org.eclipse.swt.custom.StyledText

import org.xnap.commons.i18n.I18nFactory

object I18N
{
    val i18n = I18nFactory.getI18n(getClass())
}

object MyIcon
{
    val appIcon = new Image(Display.getDefault, getClass().getResourceAsStream("/appIcon.png"))
    val ircOP = new Image(Display.getDefault, getClass().getResourceAsStream("/opIcon.png"))
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

