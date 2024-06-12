package example.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeSubcomponent
import dagger.Module
import dagger.Provides
import dagger.Reusable
import example.compose.pager.Page
import example.compose.pager.PagerSample
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Named

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appComponent = AppComponentInstance.get()
        val subcomponent = appComponent.activitySubComponentFactory().create(this)
        val mainDependencies = (subcomponent as MainDepsProvider).getMainDeps()

        val pages = listOf(
            mainDependencies.greetingPage,
            mainDependencies.sPageFirst,
            mainDependencies.sPageSecond
        )

        setContent {
            val searchValue by mainDependencies.searchFlow.collectAsState()

            PagerSample(
                items = pages,
                searchValue = searchValue,
                onSearchQuery = { newQuery ->
                    mainDependencies.searchFlow.value = newQuery
                }
            )
        }
    }
}

@ContributesTo(MainActivity::class)
interface MainDepsProvider {
    fun getMainDeps(): MainDependencies
}

class MainDependencies @Inject constructor(
    @Named("GreetingPage") val greetingPage: Page,
    @Named("SimplePageFirst") val sPageFirst: Page,
    @Named("SimplePageSecond") val sPageSecond: Page,

    val searchFlow: MutableStateFlow<String>,
)