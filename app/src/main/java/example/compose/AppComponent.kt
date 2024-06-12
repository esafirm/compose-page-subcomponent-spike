package example.compose

import com.squareup.anvil.annotations.MergeComponent

/**
 * Marker scope for App wide component
 */
interface AppScope

/**
 * A simple Dagger component component
 */
@MergeComponent(AppScope::class)
interface AppComponent {
    fun activitySubComponentFactory(): MainActivitySubComponent.Factory
}

/**
 * A singleton object that holds the AppComponent instance
 */
object AppComponentInstance {

    private var appComponent: AppComponent? = null

    fun get(): AppComponent {
        if (appComponent == null) {
            appComponent = DaggerAppComponent.create()
        }
        return appComponent!!
    }
}
