package online.devcodex.colorpeek.provider

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import online.devcodex.colorpeek.settings.ColorPeekSettings
import java.awt.Color

class CompositeElementColorProvider : ElementColorProvider {
    override fun getColorFrom(element: PsiElement): Color? =
        providerFor(element)?.getColor(element)

    override fun setColorTo(element: PsiElement, color: Color) {
        val provider = providerFor(element) ?: return
        if (ApplicationManager.getApplication().isWriteAccessAllowed) {
            provider.setColor(element, color)
        } else {
            WriteAction.run<RuntimeException> { provider.setColor(element, color) }
        }
    }

    private fun providerFor(element: PsiElement): LanguageColorProvider? {
        val enabled = service<ColorPeekSettings>().state.enabledLanguages
        return LanguageColorProvider.EP_NAME.extensionList.firstOrNull {
            it.languageId in enabled && it.getColor(element) != null
        }
    }
}
