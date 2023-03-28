package pl.llp.aircasting

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.repository.ExpandedCardsRepository
import pl.llp.aircasting.data.model.observers.AppLifecycleObserver
import pl.llp.aircasting.di.AppComponent
import pl.llp.aircasting.di.DaggerAppComponent
import pl.llp.aircasting.di.components.UserComponent
import pl.llp.aircasting.di.modules.AppModule
import pl.llp.aircasting.di.modules.DatabaseModule
import pl.llp.aircasting.di.modules.PermissionsModule
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.Settings.Companion.PREFERENCES_NAME

class AircastingApplication : Application() {
    lateinit var appComponent: AppComponent
    var userComponent: UserComponent? = null
    lateinit var permissionsModule: PermissionsModule
    lateinit var databaseModule: DatabaseModule
    lateinit var mSettings: Settings
    val settings get() = mSettings

    override fun onCreate() {
        super.onCreate()

        DatabaseProvider.setup(applicationContext)

        mSettings = Settings(getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE))
        ExpandedCardsRepository.setup(mSettings)
        setCorrectAppTheme()

        permissionsModule = PermissionsModule()
        databaseModule = DatabaseModule()

        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .permissionsModule(permissionsModule)
            .databaseModule(databaseModule)
            .build()
        appComponent.inject(this)

        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(AppLifecycleObserver())
    }

    fun onUserLoggedIn() {
        // Create an instance of UserComponent
        userComponent = appComponent
            .userComponentFactory()
            .create()
    }

    private fun setCorrectAppTheme() {
        if (mSettings.isDarkThemeEnabled()) AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_YES
        ) else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
