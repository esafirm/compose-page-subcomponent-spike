package example.compose.pager

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import example.compose.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

/**
 * [GreetingPage] and its member like [searchFlow] in its constructor, is part of the host component
 * [MainActivity] and its lifecycle is managed by the host component.
 *
 * While the view model [GreetingViewModel] and its member belongs to the page sub-component
 */
@ContributesBinding(MainActivity::class, boundType = Page::class)
@Named("GreetingPage")
class GreetingPage @Inject constructor(
    internal val searchFlow: StateFlow<String>,
) : CommonPage() {

    @Composable
    override fun Content() {
        val vm = rememberViewModel<GreetingViewModel>()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Greeting Page #1")

            Button(onClick = vm::takePicture) {
                Text(text = "Take Picture")
            }
            Button(onClick = vm::callLongTask) {
                Text(text = "Call Long Task")
            }

            val searchText by vm.searchFlow.collectAsState()
            Text(text = "Search: $searchText")
        }
    }
}

/**
 * A module to bridge parent component and child component
 */
@ContributesTo(GreetingPage::class)
@Module
class GreetingPageModule {

    @Provides
    fun provideSearchFlow(page: GreetingPage) = page.searchFlow
}

/* --------------------------------------------------- */
/* > View Model and Its Dependencies */
/* --------------------------------------------------- */

class GreetingViewModel @Inject constructor(
    private val generator: GreetingGenerator,
    private val displayer: GreetingDisplayer,
    private val scope: CoroutineScope,

    val searchFlow: StateFlow<String>,

    lifecycle: Lifecycle,
    caller: ActivityResultCaller,
) {

    private val launcher =
        caller.registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            println("VM:: take picture result: ${it.hashCode()}")
        }

    init {
        println("VM:: GreetingViewModel initiated")

        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                println("VM:: lifecycle state changed: $event")
            }
        })

        scope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                // Greet will be invoked when the page is resumed
                greet()
            }
        }
    }

    private fun greet() {
        displayer.displayGreeting(generator.generate())
    }

    fun takePicture() {
        launcher.launch()
    }

    fun callLongTask(delayInMs: Long = 10_000L) {
        val job = scope.launch {
            println("VM:: Long task started - delay: $delayInMs")
            delay(delayInMs)
            println("VM:: Long task completed inside launch")
        }
        job.invokeOnCompletion { cause ->
            println("VM:: Long task completed cause: $cause")
        }
    }
}

class GreetingGenerator @Inject constructor() {
    fun generate(): String = "Hello World"
}

class GreetingDisplayer @Inject constructor() {
    fun displayGreeting(greeting: String) = println(greeting)
}
