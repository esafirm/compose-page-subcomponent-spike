package example.compose.anvil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeSubcomponent
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import example.compose.AppScope
import example.compose.pager.GreetingViewModel
import example.compose.pager.Page
import javax.inject.Inject
import javax.inject.Named

abstract class UniquePage : Page {

    // Is there a better way to achieve unique identifier that
    // retained across configuration changes?
    abstract val identifier: String
}

/**
 * This page demonstrate a Page subcomponent that have a View Model
 * with lifecycle matching with its host [ComponentActivity] but have
 * a working [ActivityResultCaller] that can be registered in the [Page] itself (inside VM).
 */
class AnotherGreetingPage : UniquePage() {

    @Inject
    lateinit var vm: GreetingViewModel

    override val identifier: String = "GreetingPage"

    init {
        AndroidAnvilInjection.inject(this)
    }

    @Composable
    override fun Content() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Greeting Page #2")

            Button(onClick = vm::takePicture) {
                Text(text = "Take Picture")
            }
            Button(onClick = vm::callLongTask) {
                Text(text = "Call Long Task")
            }
        }
    }
}
