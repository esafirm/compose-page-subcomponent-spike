package example.compose.pager

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import java.util.concurrent.atomic.AtomicInteger

class ComposePageActivityResultCaller(
    private val registry: ActivityResultRegistry,
    private val idProvider: ResultCallerIdProvider,
    private val launcherRegistry: ResultLauncherRegistry,
) : ActivityResultCaller {

    override fun <I, O> registerForActivityResult(
        contract: ActivityResultContract<I, O>,
        callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I> {
        val key = idProvider.nextId()
        val launcher = registry.register(key, contract, callback)
        launcherRegistry.add(launcher)
        return launcher
    }

    override fun <I, O> registerForActivityResult(
        contract: ActivityResultContract<I, O>,
        registry: ActivityResultRegistry,
        callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I> {
        error("You need to use the registry provided in the constructor")
    }
}

/* --------------------------------------------------- */
/* > Id Provider */
/* --------------------------------------------------- */

class IncrementalIdProvider(
    private val prefixIdentifier: String
) : ResultCallerIdProvider {

    private val nextLocalRequestCode = AtomicInteger()

    override fun nextId(): String {
        return "${prefixIdentifier}_${nextLocalRequestCode.getAndIncrement()}"
    }
}

interface ResultCallerIdProvider {
    fun nextId(): String
}

/* --------------------------------------------------- */
/* > Registry */
/* --------------------------------------------------- */

interface ResultLauncherRegistry {
    val registeredResultCallers: Set<ActivityResultLauncher<*>>

    fun add(launcher: ActivityResultLauncher<*>)
    fun unregister()
}

class SimpleResultLauncherRegistry : ResultLauncherRegistry {
    override val registeredResultCallers: MutableSet<ActivityResultLauncher<*>> = mutableSetOf()

    @Synchronized
    override fun add(launcher: ActivityResultLauncher<*>) {
        registeredResultCallers.add(launcher)
    }

    @Synchronized
    override fun unregister() {
        registeredResultCallers.forEach { it.unregister() }
        registeredResultCallers.clear()
    }
}