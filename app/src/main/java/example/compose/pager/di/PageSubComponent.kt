package example.compose.pager.di

import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeSubcomponent
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import example.compose.AppScope
import example.compose.pager.CommonPage
import example.compose.pager.GreetingPage
import example.compose.pager.GreetingViewModel
import example.compose.pager.blocks.ResultCallerIdProvider
import example.compose.pager.blocks.ResultLauncherRegistry

/* --------------------------------------------------- */
/* > Page ViewModel Multi-Binding setup */
/* --------------------------------------------------- */

typealias PageVmProviderFactoryStore = Map<@JvmSuppressWildcards Class<*>, @JvmSuppressWildcards PageVmProvider.Factory<*>>

@ContributesTo(AppScope::class)
interface PageVmProviderFactoryStoreProvider {
    fun pageVmProviderFactoryStore(): PageVmProviderFactoryStore
}

/**
 * The base interface for all page subcomponents.
 *
 * @see [GreetingPageSubComponent]
 */
interface PageVmProvider<VM : Any> {

    fun getPageVm(): VM

    /**
     * The base interface for all page subcomponent factories.
     *
     * @see [GreetingPageSubComponent.Factory]
     */
    interface Factory<Page : CommonPage> {
        fun create(
            @BindsInstance page: Page,
            @BindsInstance activity: ComponentActivity,
            @BindsInstance idProvider: ResultCallerIdProvider,
            @BindsInstance registry: ResultLauncherRegistry,
            @BindsInstance lifecycleOwner: LifecycleOwner,
        ): PageVmProvider<*>
    }
}

/* --------------------------------------------------- */
/* > Impl -- This can be generated by anvil extension */
/* --------------------------------------------------- */

/**
 * Subcomponent implementation for [GreetingPage]
 *
 * TODO: solve the problem below:
 *
 * For now [PageVmProvider] still need the ViewModel type info to work.
 *
 * For example: `PageVmProvider<GreetingViewModel>`
 *
 * Options for now are:
 * 1. Declare the type in [example.compose.pager.CommonPage]. Ex: CommonPage<VM>.
 * 2. Declare special annotation in the view model itself that later will be processed by anvil extension.
 * 3. Ignore VM type completely. This could work but it will be remove the compile-time safety (?)
 */
@MergeSubcomponent(
    scope = GreetingPage::class,
    modules = [DefaultPageDependencies::class]
)
interface GreetingPageSubComponent : PageVmProvider<GreetingViewModel> {

    @Subcomponent.Factory
    interface Factory : PageVmProvider.Factory<GreetingPage>
}

/**
 * Contribute the [GreetingPageSubComponent.Factory] to the [AppScope].
 */
@ContributesTo(AppScope::class)
@Module(subcomponents = [GreetingPageSubComponent::class])
interface GreetingPageComponentBinder {
    @IntoMap
    @Binds
    @ClassKey(GreetingPage::class)
    fun bindGreetingComponentFactory(impl: GreetingPageSubComponent.Factory): PageVmProvider.Factory<*>
}
