package example.compose.pager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import example.compose.AppComponentInstance
import java.util.UUID

interface Page {
    @Composable
    fun Content()
}

abstract class CommonPage : Page {

    // In this implementation VM lifecycle will match the `Page.Content`
    // That means it will match the composable lifecycle
    @Composable
    protected fun <T : Any> rememberViewModel(): T {
        val pageVmProvider = rememberPageVmProvider<T>(
            pageClass = this.javaClass
        )
        return remember { pageVmProvider.getPageVm() }
    }

    // In this implementation VM lifecycle will match the host
    // But activity result caller will still function because we're using page id with `rememberSavable`
    @Composable
    protected fun <T : Any> rememberHostLifecycleViewModel(): T {
        val lifecycleOwner = LocalLifecycleOwner.current
        val pageId = rememberPageId()

        return getOrCreatePageVm(
            pageId = pageId,
            hostLifecycleOwner = lifecycleOwner,
            pageClass = this.javaClass,
        )
    }

    /**
     * A holder for [PageVmProvider] that will be created in [rememberHostLifecycleViewModel]
     */
    private var vmProviderHolder: PageVmProvider<*>? = null

    /**
     * Get or create a [PageVmProvider] that has a lifecycle matching with host
     */
    private fun <VM : Any> getOrCreatePageVm(
        pageId: String,
        hostLifecycleOwner: LifecycleOwner,
        pageClass: Class<*>,
    ): VM {
        if (vmProviderHolder == null) {
            vmProviderHolder = createPageVmProvider<VM>(pageClass, pageId, hostLifecycleOwner) {
                vmProviderHolder = null
            }
        }
        @Suppress("UNCHECKED_CAST")
        return vmProviderHolder!!.getPageVm() as VM
    }

}

/**
 * Remember a unique page id that survives configuration change
 *
 * the key for [androidx.activity.result.ActivityResultCaller] that used in [Page]
 */
@Composable
fun rememberPageId(): String {
    return rememberSaveable { UUID.randomUUID().toString() }
}

/**
 * Remember a [PageVmProvider] that has a lifecycle matching with composable
 */
@Composable
fun <VM : Any> rememberPageVmProvider(pageClass: Class<*>): PageVmProvider<VM> {
    val pageId = rememberPageId()
    val lifecycleOwner = rememberComposeLifecycleOwner()
    return remember { createPageVmProvider(pageClass, pageId, lifecycleOwner) }
}

/**
 * Create a [PageVmProvider] that has a lifecycle matching with host
 */
internal fun <VM : Any> createPageVmProvider(
    pageClass: Class<*>,
    pageId: String,
    hostLifecycleOwner: LifecycleOwner,
    onHostDestroyed: (() -> Unit)? = null,
): PageVmProvider<VM> {

    val callerRegistry = SimpleResultLauncherRegistry()
    val lifecycle = hostLifecycleOwner.lifecycle

    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            callerRegistry.unregister()
            onHostDestroyed?.invoke()
            lifecycle.removeObserver(this)
        }
    })

    val idProvider = IncrementalIdProvider(pageId)
    val appComponent = AppComponentInstance.get()

    val store = (appComponent as PageVmProviderStoreProvider).pageVmProviderStore()
    val vmProvider = store[pageClass]?.create(
        idProvider = idProvider,
        registry = callerRegistry,
        lifecycleOwner = hostLifecycleOwner
    ) ?: error("No VM factory found for $pageClass")

    @Suppress("UNCHECKED_CAST")
    return vmProvider as PageVmProvider<VM>
}

