package online.devcodex.colorpeek.provider.kotlin

import com.intellij.psi.PsiElement
import com.intellij.openapi.components.service
import online.devcodex.colorpeek.settings.ColorPeekSettings
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression
import online.devcodex.colorpeek.ColorParser
import online.devcodex.colorpeek.provider.LanguageColorProvider
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import java.awt.Color

class KotlinColorProvider : LanguageColorProvider {
    override val languageId: String = "kotlin"

    override fun getColor(element: PsiElement): Color? {
        val settings = service<ColorPeekSettings>().state
        return when (element) {
            // ColorLineMarkerProvider walks both an entry and its parent expression.
            // Advertising both creates a target-selection popup instead of opening
            // the platform color chooser directly, so the leaf entry is canonical.
            is KtStringTemplateExpression -> null
            is KtLiteralStringTemplateEntry -> if (settings.stringColors) ColorParser.parse(element.text) else null
            is KtConstantExpression -> {
                if (!settings.numericColors || element.parent is KtPrefixExpression) return null
                val color = ColorParser.parseNumber(element.text) ?: return null
                if (settings.skipComposeColors && ComposeColorAvoidance.shouldSkip(element)) null else color
            }
            else -> null
        }
    }

    override fun setColor(element: PsiElement, color: Color) {
        when (element) {
            is KtConstantExpression -> {
                val text = ColorParser.formatKotlinNumber(color, element.text, hasLongContext(element)) ?: return
                element.replace(KtPsiFactory(element.project).createExpression(text))
            }
            is KtStringTemplateExpression -> replaceExpression(element, color)
            is KtLiteralStringTemplateEntry -> {
                val expression = element.parent as? KtStringTemplateExpression ?: return
                if (expression.entries.size == 1) replaceExpression(expression, color)
            }
        }
    }

    private fun hasLongContext(element: KtConstantExpression): Boolean {
        val parent = element.parent
        val type = when (parent) {
            is org.jetbrains.kotlin.psi.KtProperty -> parent.typeReference?.text
            is org.jetbrains.kotlin.psi.KtNamedFunction -> parent.typeReference?.text
            is org.jetbrains.kotlin.psi.KtValueArgument -> {
                val call = parent.parent?.parent as? org.jetbrains.kotlin.psi.KtCallExpression ?: return false
                val name = parent.getArgumentName()?.asName?.asString()
                val index = call.valueArguments.indexOf(parent)
                val target = call.calleeExpression?.references?.firstNotNullOfOrNull { it.resolve()?.navigationElement }
                when (target) {
                    is org.jetbrains.kotlin.psi.KtFunction -> {
                        val parameter = if (name != null) target.valueParameters.find { it.name == name }
                            else target.valueParameters.getOrNull(index)
                        parameter?.typeReference?.text
                    }
                    is com.intellij.psi.PsiMethod -> target.parameterList.parameters.getOrNull(index)?.type?.canonicalText
                    else -> null
                }
            }
            else -> null
        }
        return type?.removeSuffix("?") in setOf("Long", "kotlin.Long", "long", "java.lang.Long")
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
