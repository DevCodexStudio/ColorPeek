package online.devcodex.colorpeek.provider.java

import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import online.devcodex.colorpeek.ColorParser
import online.devcodex.colorpeek.provider.LanguageColorProvider
import java.awt.Color

class JavaColorProvider : LanguageColorProvider {
    override val languageId: String = "JAVA"

    override fun getColor(element: PsiElement): Color? {
        val literal = element as? PsiLiteralExpression ?: return null
        return ColorParser.parse(literal.value as? String ?: return null)
    }

    override fun setColor(element: PsiElement, color: Color) {
        val literal = element as? PsiLiteralExpression ?: return
        val oldValue = literal.value as? String ?: return
        val newValue = ColorParser.format(color, oldValue) ?: return
        val replacement = JavaPsiFacade.getElementFactory(element.project)
            .createExpressionFromText(quoteLike(literal.text, newValue), literal)
        literal.replace(replacement)
    }

    private fun quoteLike(source: String, value: String): String {
        val start = source.indexOf('"')
        val end = source.lastIndexOf('"')
        return if (start >= 0 && end > start) source.substring(0, start + 1) + value + source.substring(end) else "\"$value\""
    }
}
