package example.compose.pager.blocks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import example.compose.pager.LocalPageInfo

/**
 * A [LifecycleOwner] that attached to a composable. Used in [CommonPage]
 */
internal class ComposeLifecycleOwner : LifecycleOwner {

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    fun dispatchEvent(event: Event) {
        lifecycleRegistry.currentState = event.targetState
    }
}

/**
 * The implementation of [CommonPage] lifecycle.
 *
 * It tries to mimic the lifecycle of a Fragment that lives in a ViewPager with ViewPagerStateAdapter.
 * At its core, it listens to the visibility of the page and dispatches lifecycle events accordingly.
 * It depends on [LocalPageInfo] and its host [LocalLifecycleOwner] to determine the visibility of the page.
 */
@Composable
internal fun rememberComposeLifecycleOwner(): ComposeLifecycleOwner {
    val hostLifecycle = LocalLifecycleOwner.current.lifecycle

    val owner = remember {
        ComposeLifecycleOwner().apply {
            dispatchEvent(Event.ON_CREATE)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            owner.dispatchEvent(Event.ON_DESTROY)
        }
    }

    val pageInfo = LocalPageInfo.current
    val isPageVisible = pageInfo.isVisible

    DisposableEffect(isPageVisible) {

        if (isPageVisible) {
            owner.dispatchEvent(Event.ON_RESUME)
        } else {
            // Not visible but previously visible
            if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                owner.dispatchEvent(Event.ON_PAUSE)
            }
        }

        // Dispatch host onResume and onPause events
        val eventObserver = LifecycleEventObserver { _, event ->
            val eligibleEvents = event == Event.ON_RESUME || event == Event.ON_PAUSE
            if (isPageVisible && eligibleEvents) {
                owner.dispatchEvent(event)
            }
        }

        // Handle host event observer subscription
        hostLifecycle.addObserver(eventObserver)
        onDispose {
            hostLifecycle.addObserver(eventObserver)
        }
    }

    return owner
}
