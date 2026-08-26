package online.devcodex.colorpeek.provider.kotlin

import com.intellij.psi.PsiElement
import online.devcodex.colorpeek.ColorParser
import online.devcodex.colorpeek.provider.LanguageColorProvider
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import java.awt.Color

class KotlinColorProvider : LanguageColorProvider {
    override val languageId: String = "kotlin"

    override fun getColor(element: PsiElement): Color? {
        return when (element) {
            // ColorLineMarkerProvider walks both an entry and its parent expression.
            // Advertising both creates a target-selection popup instead of opening
            // the platform color chooser directly, so the leaf entry is canonical.
            is KtStringTemplateExpression -> null
            is KtLiteralStringTemplateEntry -> ColorParser.parse(element.text)
            else -> null
        }
    }

    override fun setColor(element: PsiElement, color: Color) {
        when (element) {
            is KtStringTemplateExpression -> replaceExpression(element, color)
            is KtLiteralStringTemplateEntry -> {
                val expression = element.parent as? KtStringTemplateExpression ?: return
                if (expression.entries.size == 1) replaceExpression(expression, color)
            }
        }
    }

    private fun replaceExpression(expression: KtStringTemplateExpression, color: Color) {
        val oldValue = expression.singleLiteralValue() ?: return
        val newValue = ColorParser.format(color, oldValue) ?: return
        val source = expression.text
        val delimiter = if (source.startsWith("\"\"\"") && source.endsWith("\"\"\"")) "\"\"\"" else "\""
        if (!source.startsWith(delimiter) || !source.endsWith(delimiter)) return
        val replacementText = delimiter + newValue + delimiter
        expression.replace(KtPsiFactory(expression.project).createExpression(replacementText))
    }

    private fun KtStringTemplateExpression.singleLiteralValue(): String? {
        val only = entries.singleOrNull() as? KtLiteralStringTemplateEntry ?: return null
        return only.text
    }
}
