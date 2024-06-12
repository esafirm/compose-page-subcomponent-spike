//package example.compose.anvil
//
//import androidx.activity.ComponentActivity
//import androidx.activity.result.ActivityResultCaller
//import androidx.lifecycle.DefaultLifecycleObserver
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleOwner
//import androidx.lifecycle.coroutineScope
//import com.squareup.anvil.annotations.ContributesTo
//import com.squareup.anvil.annotations.MergeSubcomponent
//import dagger.Binds
//import dagger.Module
//import dagger.Provides
//import dagger.Reusable
//import dagger.Subcomponent
//import dagger.multibindings.ClassKey
//import dagger.multibindings.IntoMap
//import example.compose.AppScope
//import example.compose.pager.blocks.ComposePageActivityResultCaller
//import example.compose.pager.blocks.IncrementalIdProvider
//import example.compose.pager.Page
//import example.compose.pager.blocks.ResultCallerIdProvider
//import example.compose.pager.blocks.SimpleResultLauncherRegistry
//import example.compose.utils.SaveStateHelper
//import example.compose.utils.SaveStateHelper.Companion.createSaveStateHelper
//import kotlinx.coroutines.CoroutineScope
//import javax.inject.Named
//
//@MergeSubcomponent(
//    scope = AnotherGreetingPage::class,
//    modules = [GenericPageModule::class, BoundToHostPageModule::class]
//)
//interface AnotherPageSubComponent : AnvilAndroidInjector<AnotherGreetingPage> {
//    @Subcomponent.Factory
//    interface Factory : AnvilAndroidInjector.Factory<AnotherGreetingPage>
//}
//
///**
// * This is needed to skip direct field injection in creation time.
// */
//@ContributesTo(AppScope::class)
//@Module
//class AnotherPageProvider {
//
//    @Provides
//    @Named("AnotherGreeting")
//    fun provideAnotherPage(): Page {
//        return AnotherGreetingPage()
//    }
//}
//
//@Module
//class GenericPageModule {
//    @Provides
//    fun providePage(page: AnotherGreetingPage): UniquePage = page
//}
//
//@Module(subcomponents = [AnotherPageSubComponent::class])
//@ContributesTo(scope = AppScope::class)
//interface AnotherPageInjectorBinder {
//    @IntoMap
//    @Binds
//    @ClassKey(AnotherGreetingPage::class)
//    fun bindAnotherPageInjectorBinder(impl: AnotherPageSubComponent.Factory):
//            AnvilAndroidInjector.Factory<*>
//}
//
///* --------------------------------------------------- */
///* > Not that related */
///* --------------------------------------------------- */
//
//@Module
//class BoundToHostPageModule {
//
//    @Provides
//    fun provideLifecycle(activity: ComponentActivity): Lifecycle {
//        return activity.lifecycle
//    }
//
//    @Provides
//    fun provideLifecycleScope(lifecycle: Lifecycle): CoroutineScope {
//        return lifecycle.coroutineScope
//    }
//
//    @Reusable
//    @Provides
//    fun provideSaveStateHelper(activity: ComponentActivity): SaveStateHelper {
//        return activity.createSaveStateHelper()
//    }
//
//    @Provides
//    fun provideResultCallerIdProvider(page: UniquePage): ResultCallerIdProvider {
//        return IncrementalIdProvider(page.identifier)
//    }
//
//    @Reusable
//    @Provides
//    fun provideActivityResultCaller(
//        activity: ComponentActivity,
//        idProvider: ResultCallerIdProvider,
//    ): ActivityResultCaller {
//
//        val launcherRegistry = SimpleResultLauncherRegistry()
//
//        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
//            override fun onDestroy(owner: LifecycleOwner) {
//                launcherRegistry.unregister()
//                activity.lifecycle.removeObserver(this)
//            }
//        })
//
//        return ComposePageActivityResultCaller(
//            activity.activityResultRegistry,
//            idProvider,
//            launcherRegistry,
//        )
//    }
//}
