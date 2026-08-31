package online.devcodex.colorpeek.provider.kotlin

import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtValueArgument

/** Use symbol identity, not the spelling of Color (which may be aliased or shadowed). */
internal object ComposeColorAvoidance {
    fun shouldSkip(element: PsiElement): Boolean {
        // Resolve is unavailable during indexing: conservatively defer numeric previews.
        if (DumbService.isDumb(element.project)) return true
        var current = element.parent
        while (current != null && current !is org.jetbrains.kotlin.psi.KtDeclaration) {
            if (current is KtValueArgument) {
                val call = current.parent?.parent as? KtCallExpression
                val references = call?.calleeExpression?.references.orEmpty()
                if (references.any { reference ->
                    val target = reference.resolve()
                    isComposeColor(target) || isComposeColor(target?.navigationElement)
                }) return true
            }
            current = current.parent
        }
        return false
    }

    private fun isComposeColor(target: PsiElement?): Boolean =
        (target as? KtNamedDeclaration)?.fqName?.asString() == "androidx.compose.ui.graphics.Color"
}
