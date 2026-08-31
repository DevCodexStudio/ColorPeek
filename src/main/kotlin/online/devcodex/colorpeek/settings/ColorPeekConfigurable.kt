package online.devcodex.colorpeek.settings

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.Box
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.UIManager

class ColorPeekConfigurable : Configurable {
    private var javaCheckBox: JCheckBox? = null
    private var kotlinCheckBox: JCheckBox? = null
    private var strings: JCheckBox? = null
    private var numbers: JCheckBox? = null
    private var skipCompose: JCheckBox? = null

    override fun getDisplayName(): String = "ColorPeek"

    override fun createComponent(): JComponent {
        javaCheckBox = JCheckBox("Java")
        kotlinCheckBox = JCheckBox("Kotlin")
        strings = JCheckBox("String literals")
        numbers = JCheckBox("Hexadecimal numeric literals")
        skipCompose = JCheckBox("Prefer the IDE preview for Compose Color calls")
        numbers?.addActionListener { updateOptionAvailability() }

        return JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(4, 0)
            add(SettingsPanel().apply {
                section("Languages")
                description("Choose which source languages show ColorPeek gutter previews.")
                option(javaCheckBox!!)
                option(kotlinCheckBox!!)

                section("Color values")
                description("Choose the kinds of color values ColorPeek recognizes.")
                option(strings!!)
                detail("#RGB, #ARGB, #RRGGBB, #AARRGGBB, and equivalent 0x forms")
                option(numbers!!)
                detail("8-digit 0xAARRGGBB values only. Numeric values may also be masks or IDs.")

                section("Compatibility")
                option(skipCompose!!)
                detail("Prevents duplicate gutter icons when Android Studio or the Compose plugin already provides a color preview.")
                fill()
            }, BorderLayout.CENTER)
        }.also { reset() }
    }

    private fun updateOptionAvailability() {
        skipCompose?.isEnabled = numbers?.isSelected == true
    }

    override fun isModified(): Boolean {
        val enabled = service<ColorPeekSettings>().state.enabledLanguages
        return javaCheckBox?.isSelected != ("JAVA" in enabled) ||
            kotlinCheckBox?.isSelected != ("kotlin" in enabled) ||
            strings?.isSelected != service<ColorPeekSettings>().state.stringColors ||
            numbers?.isSelected != service<ColorPeekSettings>().state.numericColors ||
            skipCompose?.isSelected != service<ColorPeekSettings>().state.skipComposeColors
    }

    override fun apply() {
        val enabled = mutableSetOf<String>()
        if (javaCheckBox?.isSelected == true) enabled += "JAVA"
        if (kotlinCheckBox?.isSelected == true) enabled += "kotlin"
        service<ColorPeekSettings>().state.enabledLanguages = enabled
        service<ColorPeekSettings>().state.apply {
            stringColors = strings?.isSelected == true
            numericColors = numbers?.isSelected == true
            skipComposeColors = skipCompose?.isSelected == true
        }
        ProjectManager.getInstance().openProjects.forEach { DaemonCodeAnalyzer.getInstance(it).restart() }
    }

    override fun reset() {
        val enabled = service<ColorPeekSettings>().state.enabledLanguages
        javaCheckBox?.isSelected = "JAVA" in enabled
        kotlinCheckBox?.isSelected = "kotlin" in enabled
        strings?.isSelected = service<ColorPeekSettings>().state.stringColors
        numbers?.isSelected = service<ColorPeekSettings>().state.numericColors
        skipCompose?.isSelected = service<ColorPeekSettings>().state.skipComposeColors
        updateOptionAvailability()
    }

    override fun disposeUIResources() {
        javaCheckBox = null
        kotlinCheckBox = null
        strings = null
        numbers = null
        skipCompose = null
    }

    private class SettingsPanel : JPanel(GridBagLayout()) {
        private var row = 0

        fun section(title: String) {
            if (row > 0) spacer(18)
            val header = JPanel(BorderLayout(JBUI.scale(10), 0)).apply {
                isOpaque = false
                add(JLabel(title).apply { font = font.deriveFont(Font.BOLD) }, BorderLayout.WEST)
                add(JSeparator(), BorderLayout.CENTER)
            }
            addRow(header, Insets(0, 0, 7, 0))
        }

        fun description(text: String) = addRow(helpLabel(text), Insets(0, 0, 6, 0))

        fun option(checkBox: JCheckBox) = addRow(checkBox, Insets(0, 0, 2, 0))

        fun detail(text: String) = addRow(
            helpLabel(text).apply { border = JBUI.Borders.emptyLeft(24) },
            Insets(0, 0, 6, 0),
        )

        fun fill() {
            add(Box.createGlue(), constraints().apply {
                weighty = 1.0
                fill = GridBagConstraints.BOTH
            })
        }

        private fun helpLabel(text: String) = JLabel("<html><body width='620'>$text</body></html>").apply {
            foreground = UIManager.getColor("Label.disabledForeground")
        }

        private fun spacer(height: Int) =
            addRow(Box.createVerticalStrut(JBUI.scale(height)), Insets(0, 0, 0, 0))

        private fun addRow(component: Component, insets: Insets) {
            add(component, constraints().apply { this.insets = insets })
        }

        private fun constraints() = GridBagConstraints().apply {
            gridx = 0
            gridy = row++
            weightx = 1.0
            anchor = GridBagConstraints.NORTHWEST
            fill = GridBagConstraints.HORIZONTAL
        }
    }
}
