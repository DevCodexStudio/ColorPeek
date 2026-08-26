package online.devcodex.colorpeek.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "ColorPeekSettings", storages = [Storage("ColorPeek.xml")])
@Service(Service.Level.APP)
class ColorPeekSettings : PersistentStateComponent<ColorPeekSettings.State> {
    data class State(var enabledLanguages: MutableSet<String> = mutableSetOf("JAVA", "kotlin"))

    private var currentState = State()

    override fun getState(): State = currentState

    override fun loadState(state: State) {
        currentState = state
    }
}
