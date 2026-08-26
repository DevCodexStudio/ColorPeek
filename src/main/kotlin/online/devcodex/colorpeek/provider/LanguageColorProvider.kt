package online.devcodex.colorpeek.provider

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import java.awt.Color

interface LanguageColorProvider {
    val languageId: String

    fun getColor(element: PsiElement): Color?

    fun setColor(element: PsiElement, color: Color)

    companion object {
        val EP_NAME: ExtensionPointName<LanguageColorProvider> =
            ExtensionPointName.create("online.devcodex.colorpeek.languageColorProvider")
    }
}
