package online.devcodex.colorpeek.settings

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import java.awt.BorderLayout
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class ColorPeekConfigurable : Configurable {
    private var javaCheckBox: JCheckBox? = null
    private var kotlinCheckBox: JCheckBox? = null

    override fun getDisplayName(): String = "ColorPeek"

    override fun createComponent(): JComponent {
        javaCheckBox = JCheckBox("Java")
        kotlinCheckBox = JCheckBox("Kotlin")
        return JPanel(BorderLayout()).apply {
            add(JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                add(JLabel("Show color previews in:"))
                add(javaCheckBox)
                add(kotlinCheckBox)
            }, BorderLayout.NORTH)
        }.also { reset() }
    }

    override fun isModified(): Boolean {
        val enabled = service<ColorPeekSettings>().state.enabledLanguages
        return javaCheckBox?.isSelected != ("JAVA" in enabled) ||
            kotlinCheckBox?.isSelected != ("kotlin" in enabled)
    }

    override fun apply() {
        val enabled = mutableSetOf<String>()
        if (javaCheckBox?.isSelected == true) enabled += "JAVA"
        if (kotlinCheckBox?.isSelected == true) enabled += "kotlin"
        service<ColorPeekSettings>().state.enabledLanguages = enabled
        ProjectManager.getInstance().openProjects.forEach { DaemonCodeAnalyzer.getInstance(it).restart() }
    }

    override fun reset() {
        val enabled = service<ColorPeekSettings>().state.enabledLanguages
        javaCheckBox?.isSelected = "JAVA" in enabled
        kotlinCheckBox?.isSelected = "kotlin" in enabled
    }

    override fun disposeUIResources() {
        javaCheckBox = null
        kotlinCheckBox = null
    }
}
