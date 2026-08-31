package online.devcodex.colorpeek.provider.java

import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiPrefixExpression
import com.intellij.openapi.components.service
import online.devcodex.colorpeek.settings.ColorPeekSettings
import online.devcodex.colorpeek.ColorParser
import online.devcodex.colorpeek.provider.LanguageColorProvider
import java.awt.Color

class JavaColorProvider : LanguageColorProvider {
    override val languageId: String = "JAVA"

    override fun getColor(element: PsiElement): Color? {
        val literal = element as? PsiLiteralExpression ?: return null
        val settings = service<ColorPeekSettings>().state
        val value = literal.value
        return when {
            value is String && settings.stringColors -> ColorParser.parse(value)
            value is Number && settings.numericColors && literal.parent !is PsiPrefixExpression -> ColorParser.parseNumber(literal.text)
            else -> null
        }
    }

    override fun setColor(element: PsiElement, color: Color) {
        val literal = element as? PsiLiteralExpression ?: return
        if (literal.value is Number) {
            val text = ColorParser.formatNumber(color, literal.text) ?: return
            literal.replace(JavaPsiFacade.getElementFactory(element.project).createExpressionFromText(text, literal))
            return
        }
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
