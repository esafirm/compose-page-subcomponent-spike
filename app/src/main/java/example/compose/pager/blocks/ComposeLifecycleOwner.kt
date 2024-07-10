package example.compose.pager.blocks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import example.compose.pager.LocalPageInfo

class ComposeLifecycleOwner : LifecycleOwner {

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    fun dispatchEvent(event: Lifecycle.Event) {
        lifecycleRegistry.currentState = event.targetState
    }
}

@Composable
internal fun rememberComposeLifecycleOwner(): ComposeLifecycleOwner {
    val pageInfo = LocalPageInfo.current

    val owner = remember {
        ComposeLifecycleOwner().apply {
            dispatchEvent(Lifecycle.Event.ON_CREATE)
        }
    }

    LaunchedEffect(pageInfo) {
        val isVisible = pageInfo.isVisible
        if (isVisible) {
            owner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        } else {
            // Not visible and previously visible
            if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                owner.dispatchEvent(Lifecycle.Event.ON_PAUSE)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            owner.dispatchEvent(Lifecycle.Event.ON_DESTROY)
        }
    }

    return owner
}
