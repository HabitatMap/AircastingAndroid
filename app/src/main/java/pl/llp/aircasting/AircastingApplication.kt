package pl.llp.aircasting

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner

import pl.llp.aircasting.data.local.repository.ExpandedCardsRepository
import pl.llp.aircasting.data.model.observers.AppLifecycleObserver
import pl.llp.aircasting.di.UserDependentComponent
import pl.llp.aircasting.di.components.AppComponent
import pl.llp.aircasting.di.components.DaggerAppComponent
import pl.llp.aircasting.di.modules.AppModule
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.Settings.Companion.PREFERENCES_NAME

open class AircastingApplication : Application() {
    var userDependentComponent: UserDependentComponent? = null
    lateinit var appComponent: AppComponent
    lateinit var mSettings: Settings
    val settings get() = mSettings

    override fun onCreate() {
        super.onCreate()

        mSettings = Settings(getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE))
        ExpandedCardsRepository.setup(mSettings)
        setCorrectAppTheme()

        appComponent = initialiseAppComponent()
        appComponent.inject(this)

        if (mSettings.getAuthToken() != null) {
            onUserLoggedIn()
        }

        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(AppLifecycleObserver())
    }

    protected open fun initialiseAppComponent(): AppComponent = DaggerAppComponent.builder()
        .appModule(AppModule(this))
        .build()

    fun onUserLoggedIn() {
        // Create an instance of UserComponent
        userDependentComponent = initialiseUserDependentComponent()
    }

    protected open fun initialiseUserDependentComponent(): UserDependentComponent = appComponent
        .userComponentFactory()
        .create()

    private fun setCorrectAppTheme() {
        if (mSettings.isDarkThemeEnabled()) AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_YES
        ) else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
