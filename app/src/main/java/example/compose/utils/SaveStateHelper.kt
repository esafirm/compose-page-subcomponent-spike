package example.compose.utils

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistry
import example.compose.utils.SaveStateHelper.Saver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface SaveStateHelper {

    /**
     *  Get state as [StateProperty] by property delegation
     */
    fun <T : Int?> stateFlow(defaultValue: T): ReadOnlyProperty<Any, StateProperty<T>>
    fun <T : Double?> stateFlow(defaultValue: T): ReadOnlyProperty<Any, StateProperty<T>>
    fun <T : Boolean?> stateFlow(defaultValue: T): ReadOnlyProperty<Any, StateProperty<T>>
    fun <T : String?> stateFlow(defaultValue: T): ReadOnlyProperty<Any, StateProperty<T>>
    fun <T : Serializable?> stateFlow(defaultValue: T): ReadOnlyProperty<Any, StateProperty<T>>
    fun <T : Parcelable?> stateFlow(defaultValue: T): ReadOnlyProperty<Any, StateProperty<T>>

    /**
     *  Functions that allow us to pass key manually, would be useful for dynamic keys.
     */
    fun <T : Int?> stateFlow(key: String, defaultValue: T): StateProperty<T>
    fun <T : Double?> stateFlow(key: String, defaultValue: T): StateProperty<T>
    fun <T : Boolean?> stateFlow(key: String, defaultValue: T): StateProperty<T>
    fun <T : String?> stateFlow(key: String, defaultValue: T): StateProperty<T>
    fun <T : Serializable?> stateFlow(key: String, defaultValue: T): StateProperty<T>
    fun <T : Parcelable?> stateFlow(key: String, defaultValue: T): StateProperty<T>

    /**
     *  Deal with the situation when you have something that is not serializable to save,
     *  and you want to provide a customized solution to save/ restore the value.
     */
    fun <T : Any> withSaver(
        defaultValue: T,
        saver: Saver<Any?, T>,
    ): ReadOnlyProperty<Any, StateProperty<T>>

    fun <T : Any> withSaver(
        key: String,
        defaultValue: T,
        saver: Saver<Any?, T>,
    ): StateProperty<T>

    /**
     *  Indicates whether the screen is being restored or fresh created.
     */
    suspend fun isRestored(): Boolean

    /**
     *  To save/ restore a non-serializable type.
     */
    interface Saver<Savable : Any?, T> {
        fun save(value: T): Savable
        fun restore(from: Savable): T?
    }

    companion object {

        fun ComponentActivity.createSaveStateHelper(): SaveStateHelper {
            return SaveStateHelperImpl(savedStateRegistry, lifecycle)
        }
    }
}

/**
 *  Helper class that makes save state more easier and less mistaken. Please remember to scope this
 *  dependency to either activity or fragment, in case you use it in more than one place and the
 *  state restoration end up being incorrect.
 */
internal class SaveStateHelperImpl(
    private val savedStateRegistry: SavedStateRegistry,
    lifecycle: Lifecycle,
) : SaveStateHelper {

    private val stateMappingKey get() = "stateMapping"

    private val stateProperties = ConcurrentHashMap<String, StateProperty<Any?>>()

    private val restoredState = AtomicReference<Bundle?>(null)

    private val initState = MutableStateFlow(InitState.NotInited)

    init {
        if (savedStateRegistry.getSavedStateProvider(stateMappingKey) != null) {
            Log.e("SS::", "Save bundle: save state provider is already registered")
            savedStateRegistry.unregisterSavedStateProvider(stateMappingKey)
        }

        savedStateRegistry.registerSavedStateProvider(stateMappingKey, ::generateSaveStateBundle)

        // Restore state for existing stateFlows after lifecycle is at least in Created state
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                performStateRestoration()
                lifecycle.removeObserver(this)
            }
        })

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                savedStateRegistry.unregisterSavedStateProvider(stateMappingKey)
            }
        })
    }

    /**
     *  Get state as [StateProperty] by property delegation
     */
    override fun <T : Int?> stateFlow(defaultValue: T): ReadOnlyProperty<Any, StateProperty<T>> {
        return SaveStatePropertyDelegate(defaultValue)
    }

    override fun <T : Double?> stateFlow(defaultValue: T): ReadOnlyProperty<Any, StateProperty<T>> {
        return SaveStatePropertyDelegate(defaultValue)
    }

    override fun <T : Boolean?> stateFlow(defaultValue: T): ReadOnlyProperty<Any, StateProperty<T>> {
        return SaveStatePropertyDelegate(defaultValue)
    }

    override fun <T : String?> stateFlow(defaultValue: T): ReadOnlyProperty<Any, StateProperty<T>> {
        return SaveStatePropertyDelegate(defaultValue)
    }

    override fun <T : Serializable?> stateFlow(defaultValue: T): ReadOnlyProperty<Any, StateProperty<T>> {
        return SaveStatePropertyDelegate(defaultValue)
    }

    override fun <T : Parcelable?> stateFlow(defaultValue: T): ReadOnlyProperty<Any, StateProperty<T>> {
        return SaveStatePropertyDelegate(defaultValue)
    }

    /**
     *  Functions that allow us to pass key manually, would be useful for dynamic keys.
     */
    override fun <T : Int?> stateFlow(key: String, defaultValue: T): StateProperty<T> {
        return getStateProperty(key, defaultValue)
    }

    override fun <T : Double?> stateFlow(key: String, defaultValue: T): StateProperty<T> {
        return getStateProperty(key, defaultValue)
    }

    override fun <T : Boolean?> stateFlow(key: String, defaultValue: T): StateProperty<T> {
        return getStateProperty(key, defaultValue)
    }

    override fun <T : String?> stateFlow(key: String, defaultValue: T): StateProperty<T> {
        return getStateProperty(key, defaultValue)
    }

    override fun <T : Serializable?> stateFlow(key: String, defaultValue: T): StateProperty<T> {
        return getStateProperty(key, defaultValue)
    }

    override fun <T : Parcelable?> stateFlow(key: String, defaultValue: T): StateProperty<T> {
        return getStateProperty(key, defaultValue)
    }

    override fun <T : Any> withSaver(
        defaultValue: T,
        saver: Saver<Any?, T>,
    ): ReadOnlyProperty<Any, StateProperty<T>> {
        return SaveStatePropertyDelegate(defaultValue, saver)
    }

    override fun <T : Any> withSaver(
        key: String,
        defaultValue: T,
        saver: Saver<Any?, T>,
    ): StateProperty<T> {
        return getStateProperty(key, defaultValue, saver)
    }

    override suspend fun isRestored(): Boolean {
        return initState
            .mapNotNull { state ->
                when (state) {
                    InitState.NotInited -> null
                    InitState.Inited -> false
                    InitState.Restored -> true
                }
            }
            .first()
    }

    private fun generateSaveStateBundle(): Bundle {
        return Bundle().apply {
            stateProperties.forEach { (key, flow) ->
                putProperty(key, flow)
            }
            Log.d("SS::", "Save bundle: $this")
        }
    }

    private fun <T> Bundle.putProperty(key: String, property: StateProperty<T?>) {
        val value = if (property.saver == null) {
            property.value
        } else {
            property.saver.save(property.value)
        } ?: return

        when (value) {
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is String -> putString(key, value)
            is Double -> putDouble(key, value)
            is Parcelable -> putParcelable(key, value)
            is Serializable -> putSerializable(key, value)
            else -> error("${value::class.qualifiedName} type isn't supported yet, please add it")
        }
    }

    private fun performStateRestoration() {
        val restoredBundle = savedStateRegistry.consumeRestoredStateForKey(stateMappingKey)

        // consume state can be `null` (in case of Fragment)
        val bundle = restoredBundle ?: Bundle.EMPTY
        restoredState.set(bundle)

        stateProperties.forEach { (key, property) ->
            property.restoreState(bundle, key)
        }

        initState.value = if (restoredBundle == null) InitState.Inited else InitState.Restored
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getStateProperty(
        key: String,
        defaultValue: T,
        saver: Saver<Any?, T>? = null,
    ): StateProperty<T> {
        val property = stateProperties[key]

        if (property != null) {
            return property as StateProperty<T>
        }

        stateProperties[key] = StateProperty(defaultValue, saver) as StateProperty<Any?>

        // Try to retrieve state from bundle
        val bundle = restoredState.get()
        if (bundle != null) {
            stateProperties[key]?.restoreState(bundle, key)
        }

        return stateProperties[key] as StateProperty<T>
    }

    private inner class SaveStatePropertyDelegate<T>(
        private val defaultValue: T,
        private val saver: Saver<Any?, T>? = null,
    ) : ReadOnlyProperty<Any?, StateProperty<T>> {

        override fun getValue(thisRef: Any?, property: KProperty<*>): StateProperty<T> {
            return getStateProperty(property.name, defaultValue, saver)
        }
    }

    private enum class InitState {
        NotInited, Inited, Restored
    }
}

class StateProperty<T>(
    internal val defaultValue: T,
    internal val saver: Saver<Any?, T>? = null,
) {

    private val _isInitialized = AtomicBoolean(false)
    private val _flow = MutableSharedFlow<T>(
        replay = 1,
        extraBufferCapacity = 1,
    )

    internal val isInitialized: Boolean
        get() = _isInitialized.get()

    var value: T = defaultValue
        set(value) {
            // mark that property is initialized
            _isInitialized.set(true)
            field = value
            _flow.tryEmit(field)
        }

    val flow: Flow<T> get() = _flow
}

private fun <T> StateProperty<T>.restoreState(bundle: Bundle, key: String) {
    // ignore initial value, because value already updated (probably set was called before init)
    if (!isInitialized) {
        // Suppress the deprecation for Bundle.get() function for now, there is no alternative
        // API proposed that fit the generic needs, waiting for this issue to be resolved.
        // https://issuetracker.google.com/issues/243119654
        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        value = if (saver == null) {
            bundle.get(key) as? T ?: defaultValue
        } else {
            saver.restore(bundle.get(key)) ?: defaultValue
        }
    }
}

/**
 * @return [StateProperty.value] or throw
 */
fun <T : Any> StateProperty<T?>.requireValue(): T =
    checkNotNull(value) { "value is null: $this" }

fun <T> StateProperty<T>.stateIn(
    coroutineScope: CoroutineScope,
    started: SharingStarted = SharingStarted.WhileSubscribed(),
): StateFlow<T> {
    return flow.stateIn(coroutineScope, started, value)
}
