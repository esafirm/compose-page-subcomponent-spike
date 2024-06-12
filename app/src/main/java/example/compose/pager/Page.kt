package example.compose.pager

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import example.compose.AppComponentInstance
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

interface Page {
    @Composable
    fun Content()
}

abstract class CommonPage : Page {

    // In this implementation VM lifecycle will match the `Page.Content`
    // That means it will match the composable lifecycle
    @Composable
    inline fun <reified T : Any> rememberViewModel(): T {
        val factoryProvider = rememberPageComponent() as PageVmFactoryProvider
        return remember {
            factoryProvider.getFactory()[T::class.java] as T
        }
    }

    // In this implementation VM lifecycle will match the host
    // But activity result caller will still function because we're using page id with `rememberSavable`
    @Composable
    inline fun <reified T : Any> rememberHostLifecycleViewModel(): T {
        val pageId = rememberPageId()
        val lifecycleOwner = LocalLifecycleOwner.current

        return getOrCreatePageVm(
            pageId = pageId,
            hostLifecycleOwner = lifecycleOwner,
            clazz = T::class.java
        )
    }

    companion object {
        // Since this is static, it will be there as long as the app is running
        internal val pageVmStore: MutableMap<String, PageVmFactory> = mutableMapOf()
    }
}

fun <T : Any> getOrCreatePageVm(
    pageId: String,
    hostLifecycleOwner: LifecycleOwner,
    clazz: Class<T>,
): T {
    val pageVm = CommonPage.pageVmStore[pageId]
    if (pageVm == null) {

        val component = createHostLifecyclePageComponent(pageId, hostLifecycleOwner)
        val factoryProvider = component as PageVmFactoryProvider

        val lifecycle = hostLifecycleOwner.lifecycle
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                CommonPage.pageVmStore.remove(pageId)
                lifecycle.removeObserver(this)
            }
        })

        CommonPage.pageVmStore[pageId] = factoryProvider.getFactory()
    }
    return CommonPage.pageVmStore[pageId]!!.getVm(clazz)
}

internal fun createHostLifecyclePageComponent(
    pageId: String,
    hostLifecycleOwner: LifecycleOwner,
): PageSubComponent {
    val callerRegistry = SimpleResultLauncherRegistry()
    val lifecycle = hostLifecycleOwner.lifecycle

    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            callerRegistry.unregister()
            lifecycle.removeObserver(this)
        }
    })

    val idProvider = IncrementalIdProvider(pageId)

    return AppComponentInstance.get().pageComponentFactory().create(
        idProvider = idProvider,
        registry = callerRegistry,
        lifecycleOwner = hostLifecycleOwner
    )
}

internal fun <T : Any> PageVmFactory.getVm(clazz: Class<T>): T {
    @Suppress("UNCHECKED_CAST")
    return this[clazz] as T
}

@Composable
fun rememberPageId(): String {
    return rememberSaveable { UUID.randomUUID().toString() }
}

@Composable
fun rememberPageComponent(): PageSubComponent {
    val prefixIdentifier = rememberSaveable { UUID.randomUUID().toString() }
    val idProvider = remember { IncrementalIdProvider(prefixIdentifier) }
    val callerRegistry = remember { SimpleResultLauncherRegistry() }

    DisposableEffect(prefixIdentifier, idProvider) {
        onDispose {
            callerRegistry.unregister()
        }
    }

    val lifecycleOwner = rememberComposeLifecycleOwner()

    return remember {
        AppComponentInstance.get().pageComponentFactory().create(
            idProvider = idProvider,
            registry = callerRegistry,
            lifecycleOwner = lifecycleOwner
        )
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

