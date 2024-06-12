package example.compose

import androidx.activity.ComponentActivity
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeSubcomponent
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.Subcomponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Simulate host layer component from `@ContributesInjector`
 */
@MergeSubcomponent(MainActivity::class)
interface MainActivitySubComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance mainActivity: MainActivity): MainActivitySubComponent
    }
}

/**
 * A module that convert the bounded activity to a generic [ComponentActivity]
 */
@Module(subcomponents = [MainActivitySubComponent::class])
class GenericActivityModule {
    @Provides
    fun provideActivity(activity: MainActivity): ComponentActivity = activity
}


@ContributesTo(MainActivity::class)
@Module
class SearchModule {
    @Reusable
    @Provides
    fun provideMutableSearchFlow(): MutableStateFlow<String> = MutableStateFlow("")

    @Provides
    fun provideSearchFlow(mutableFlow: MutableStateFlow<String>): StateFlow<String> = mutableFlow

}
