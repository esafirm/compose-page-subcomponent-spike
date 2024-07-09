package example.compose.pager.blocks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class ComposeLifecycleOwner : LifecycleOwner {

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    fun moveToState(state: Lifecycle.State) {
        lifecycleRegistry.currentState = state
    }
}

@Composable
fun rememberComposeLifecycleOwner(): ComposeLifecycleOwner {
    val lifecycleOwner = remember {
        ComposeLifecycleOwner().apply {
            moveToState(Lifecycle.State.INITIALIZED)
        }
    }

    // I'm thinking, customized composable lifecycle is probably something that we need,
    // but I'm not sure it will be reliable as there is probably no way to provide a "full"
    // lifecycle with for example onPause event. This makes me think that probably using
    // fragment as a wrapper would still be a better choice given current situation.
    DisposableEffect(Unit) {
        lifecycleOwner.moveToState(Lifecycle.State.STARTED)
        lifecycleOwner.moveToState(Lifecycle.State.RESUMED)
        onDispose {
            lifecycleOwner.moveToState(Lifecycle.State.DESTROYED)
        }
    }

    return lifecycleOwner
}
