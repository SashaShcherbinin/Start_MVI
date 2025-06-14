@file:OptIn(ExperimentalCoroutinesApi::class)

package base.compose.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlin.reflect.KClass

interface Mvi<INTENT, EVENT, STATE> {
    val state: Flow<STATE>
    val event: Flow<EVENT>

    fun process(intent: INTENT)
}

interface Intent
interface Effect
interface Event
interface State

interface Processor<INTENT : Intent, EFFECT : Effect, STATE : State> {
    fun process(intent: INTENT, state: STATE): Flow<EFFECT>
}

interface Reducer<EFFECT : Effect, STATE : State> {
    fun reduce(effect: EFFECT, state: STATE): STATE?
}

interface Publisher<EFFECT : Effect, EVENT : Event> {
    fun publish(effect: EFFECT): EVENT?
}

interface Repeater<EFFECT : Effect, INTENT : Intent, STATE : State> {
    fun repeat(effect: EFFECT, state: STATE): INTENT?
}

open class MviViewModel<INTENT : Intent, EFFECT : Effect, EVENT : Event, STATE : State>(
    defaultState: STATE,
    private val processor: Processor<INTENT, EFFECT, STATE>,
    private val reducer: Reducer<EFFECT, STATE>,
    private val publisher: Publisher<EFFECT, EVENT>? = null,
    private val repeater: Repeater<EFFECT, INTENT, STATE>? = null,
) : ViewModel(), Mvi<INTENT, EVENT, STATE> {

    private val _state: MutableStateFlow<STATE> = MutableStateFlow(defaultState)
    private val _event: MutableSharedFlow<Consumable<EVENT>> = MutableSharedFlow(replay = 1000)

    private val intentJobMap = mutableMapOf<KClass<out INTENT>, Job>()

    override val state = _state.asStateFlow()
    override val event = _event
        .onCompletion { _event.resetReplayCache() }
        .mapNotNull { it.consume() }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
        )

    override fun process(intent: INTENT) {
        intentJobMap[intent::class]?.cancel()
        val job = processor.process(intent, _state.value)
            .onEach { effect: EFFECT ->
                reducer.reduce(effect, _state.value)?.let { _state.value = it }
                publisher?.publish(effect)?.let { event ->
                    _event.emit(Consumable(event))
                }
                repeater?.repeat(effect, _state.value)?.let { process(it) }
            }
            .launchIn(viewModelScope)
        intentJobMap[intent::class] = job
    }
}

class Consumable<T>(private var value: T?) {
    fun consume(): T? {
        val result = value
        value = null
        return result
    }
}
