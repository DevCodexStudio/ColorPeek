package online.devcodex.colorpeek

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import online.devcodex.colorpeek.provider.CompositeElementColorProvider
import online.devcodex.colorpeek.settings.ColorPeekSettings
import online.devcodex.colorpeek.settings.ColorPeekConfigurable
import org.jetbrains.kotlin.psi.KtConstantExpression
import java.awt.Color
import java.awt.Container
import javax.swing.JCheckBox
import javax.swing.JLabel

class NumericColorProviderTest : BasePlatformTestCase() {
    private val provider = CompositeElementColorProvider()

    override fun tearDown() {
        try {
            service<ColorPeekSettings>().loadState(ColorPeekSettings.State())
        } finally {
            super.tearDown()
        }
    }

    fun testKotlinOptInAndWriteBack() {
        val file = myFixture.configureByText("Example.kt", "val color = 0xFF_E2_AA_8AuL.toInt()")
        val literal = PsiTreeUtil.findChildOfType(file, KtConstantExpression::class.java)!!
        assertNull(provider.getColorFrom(literal))
        service<ColorPeekSettings>().state.numericColors = true
        assertEquals(Color(0xe2, 0xaa, 0x8a), provider.getColorFrom(literal))
        WriteCommandAction.runWriteCommandAction(project) { provider.setColorTo(literal, Color(1, 2, 3, 0x80)) }
        assertEquals("val color = 0x80_01_02_03uL.toInt()", file.text)
        val updated = PsiTreeUtil.findChildOfType(file, KtConstantExpression::class.java)!!
        assertEquals(Color(1, 2, 3, 0x80), provider.getColorFrom(updated))
        service<ColorPeekSettings>().state.enabledLanguages.clear()
        assertNull(provider.getColorFrom(updated))
    }

    fun testJavaNumberAndStringControls() {
        val file = myFixture.configureByText("Example.java", "class Example { long color = 0Xff112233L; String s = \"#112233\"; }")
        val literals = PsiTreeUtil.collectElementsOfType(file, PsiLiteralExpression::class.java).toList()
        assertNull(provider.getColorFrom(literals[0]))
        assertNotNull(provider.getColorFrom(literals[1]))
        service<ColorPeekSettings>().state.apply { numericColors = true; stringColors = false }
        assertNull(provider.getColorFrom(literals[1]))
        assertNotNull(provider.getColorFrom(literals[0]))
        WriteCommandAction.runWriteCommandAction(project) { provider.setColorTo(literals[0], Color(0xab, 0xcd, 0xef, 0x80)) }
        assertTrue(file.text.contains("0X80abcdefL"))
    }

    fun testCustomColorIsNotSkipped() {
        val file = myFixture.configureByText("Example.kt", "fun Color(value: Long) = value\nval x = Color(0xFF112233)")
        service<ColorPeekSettings>().state.numericColors = true
        val literal = PsiTreeUtil.findChildOfType(file, KtConstantExpression::class.java)!!
        assertNotNull(provider.getColorFrom(literal))
    }

    fun testExplicitLongContextSurvivesAlphaEdit() {
        val file = myFixture.configureByText("Example.kt", "val x: Long = 0x00112233")
        service<ColorPeekSettings>().state.numericColors = true
        val literal = PsiTreeUtil.findChildOfType(file, KtConstantExpression::class.java)!!
        WriteCommandAction.runWriteCommandAction(project) { provider.setColorTo(literal, Color(0x11, 0x22, 0x33)) }
        assertEquals("val x: Long = 0xFF112233", file.text)
    }

    fun testComposeAndAliasAvoidance() {
        myFixture.addFileToProject("androidx/compose/ui/graphics/Color.kt", "package androidx.compose.ui.graphics\nfun Color(value: Long): Long = value")
        val file = myFixture.configureByText("Example.kt", "import androidx.compose.ui.graphics.Color as ComposeColor\nval x = ComposeColor(0xFF112233)")
        service<ColorPeekSettings>().state.numericColors = true
        val literal = PsiTreeUtil.findChildOfType(file, KtConstantExpression::class.java)!!
        assertNull(provider.getColorFrom(literal))
        service<ColorPeekSettings>().state.skipComposeColors = false
        assertNotNull(provider.getColorFrom(literal))
    }

    fun testFullyQualifiedComposeAndConvertedArgument() {
        myFixture.addFileToProject("androidx/compose/ui/graphics/Color.kt", "package androidx.compose.ui.graphics\nfun Color(value: Int): Int = value")
        val file = myFixture.configureByText("Example.kt", "val x = androidx.compose.ui.graphics.Color(0xFF112233.toInt())")
        service<ColorPeekSettings>().state.numericColors = true
        val literal = PsiTreeUtil.findChildOfType(file, KtConstantExpression::class.java)!!
        assertNull(provider.getColorFrom(literal))
    }

    fun testSettingsRoundTripAndLegacyDefaults() {
        val settings = service<ColorPeekSettings>()
        val defaults = ColorPeekSettings.State()
        assertTrue(defaults.stringColors)
        assertFalse(defaults.numericColors)
        assertTrue(defaults.skipComposeColors)
        settings.loadState(ColorPeekSettings.State(mutableSetOf("kotlin"), false, true, false))
        val xml = com.intellij.util.xmlb.XmlSerializer.serialize(settings.state)
        val loaded = com.intellij.util.xmlb.XmlSerializer.deserialize(xml, ColorPeekSettings.State::class.java)
        assertEquals(settings.state, loaded)
        val oldXml = org.jdom.Element("State")
        assertEquals(defaults, com.intellij.util.xmlb.XmlSerializer.deserialize(oldXml, ColorPeekSettings.State::class.java))
    }

    fun testSettingsPageStructureAndOptionDependency() {
        val configurable = ColorPeekConfigurable()
        try {
            val component = configurable.createComponent()
            val labels = descendants(component).filterIsInstance<JLabel>().map { it.text }.toSet()
            assertTrue("Languages" in labels)
            assertTrue("Color values" in labels)
            assertTrue("Compatibility" in labels)

            val checkBoxes = descendants(component).filterIsInstance<JCheckBox>().associateBy { it.text }
            val numeric = checkBoxes.getValue("Hexadecimal numeric literals")
            val compose = checkBoxes.getValue("Prefer the IDE preview for Compose Color calls")
            assertFalse(numeric.isSelected)
            assertFalse(compose.isEnabled)
            numeric.doClick()
            assertTrue(compose.isEnabled)
        } finally {
            configurable.disposeUIResources()
        }
    }

    private fun descendants(root: java.awt.Component): Sequence<java.awt.Component> = sequence {
        yield(root)
        if (root is Container) root.components.forEach { yieldAll(descendants(it)) }
    }
}
