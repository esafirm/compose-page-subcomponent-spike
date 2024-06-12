package example.compose.pager.di

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import dagger.Module
import dagger.Provides
import dagger.Reusable
import example.compose.pager.blocks.ComposePageActivityResultCaller
import example.compose.pager.blocks.ResultCallerIdProvider
import example.compose.pager.blocks.ResultLauncherRegistry
import kotlinx.coroutines.CoroutineScope

/**
 * Provides default dependencies for a [example.compose.pager.CommonPage].
 */
@Module
class DefaultPageDependencies {

    @Provides
    fun provideLifecycle(lifecycleOwner: LifecycleOwner): Lifecycle {
        return lifecycleOwner.lifecycle
    }

    @Provides
    fun provideLifecycleScope(lifecycleOwner: LifecycleOwner): CoroutineScope {
        return lifecycleOwner.lifecycle.coroutineScope
    }

    @Reusable
    @Provides
    fun provideActivityResultCaller(
        activity: ComponentActivity,
        idProvider: ResultCallerIdProvider,
        registry: ResultLauncherRegistry,
    ): ActivityResultCaller {
        return ComposePageActivityResultCaller(
            activity.activityResultRegistry,
            idProvider,
            registry
        )
    }
}
