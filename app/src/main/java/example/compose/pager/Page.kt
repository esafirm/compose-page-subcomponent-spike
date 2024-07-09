package example.compose.pager

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import example.compose.AppComponentInstance
import example.compose.pager.blocks.IncrementalIdProvider
import example.compose.pager.blocks.SimpleResultLauncherRegistry
import example.compose.pager.blocks.rememberComposeLifecycleOwner
import example.compose.pager.di.PageVmProvider
import example.compose.pager.di.PageVmProviderFactoryStoreProvider
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
        val pageVmProvider = rememberPageVmProvider<T>(this)
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
            page = this,
        )
    }

    /**
     * A holder for [PageVmProvider] that will be created in [rememberHostLifecycleViewModel]
     */
    private var vmHolder: Any? = null

    /**
     * Get or create a [PageVmProvider] that has a lifecycle matching with host
     */
    private fun <VM : Any> getOrCreatePageVm(
        page: CommonPage,
        pageId: String,
        hostLifecycleOwner: LifecycleOwner,
    ): VM {
        if (vmHolder == null) {
            val pageVmProvider = createPageVmProvider<VM>(
                page = page,
                pageId = pageId,
                host = hostLifecycleOwner as ComponentActivity,
                lifecycleOwner = hostLifecycleOwner,
            ) {
                vmHolder = null
            }
            vmHolder = pageVmProvider.getPageVm()
        }
        @Suppress("UNCHECKED_CAST")
        return vmHolder as VM
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
fun <VM : Any> rememberPageVmProvider(page: CommonPage): PageVmProvider<VM> {
    val pageId = rememberPageId()
    val lifecycleOwner = rememberComposeLifecycleOwner()
    val host = LocalContext.current as ComponentActivity
    return remember {
        createPageVmProvider(
            page = page,
            pageId = pageId,
            lifecycleOwner = lifecycleOwner,
            host = host,
        )
    }
}

/**
 * Create a [PageVmProvider] that has a lifecycle matching with host
 */
@Suppress("UNCHECKED_CAST")
internal fun <VM : Any> createPageVmProvider(
    page: CommonPage,
    pageId: String,
    host: ComponentActivity,
    lifecycleOwner: LifecycleOwner,
    onHostDestroyed: (() -> Unit)? = null,
): PageVmProvider<VM> {

    val callerRegistry = SimpleResultLauncherRegistry()
    val lifecycle = lifecycleOwner.lifecycle

    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            callerRegistry.unregister()
            onHostDestroyed?.invoke()
            lifecycle.removeObserver(this)
        }
    })

    val idProvider = IncrementalIdProvider(pageId)
    val appComponent = AppComponentInstance.get()

    val pageClass = page.javaClass
    val store = (appComponent as PageVmProviderFactoryStoreProvider).pageVmProviderFactoryStore()

    val factory = store[pageClass] as? PageVmProvider.Factory<CommonPage>
    val vmProvider = factory?.create(
        page = page,
        activity = host,
        idProvider = idProvider,
        registry = callerRegistry,
        lifecycleOwner = lifecycleOwner
    ) ?: error("No VM factory found for $pageClass")

    @Suppress("UNCHECKED_CAST")
    return vmProvider as PageVmProvider<VM>
}
