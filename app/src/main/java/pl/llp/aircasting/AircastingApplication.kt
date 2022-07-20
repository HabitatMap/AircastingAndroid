package pl.llp.aircasting

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.local.repository.ExpandedCardsRepository
import pl.llp.aircasting.data.model.observers.AppLifecycleObserver
import pl.llp.aircasting.di.AppComponent
import pl.llp.aircasting.di.DaggerAppComponent
import pl.llp.aircasting.di.modules.AppModule
import pl.llp.aircasting.di.modules.DatabaseModule
import pl.llp.aircasting.di.modules.PermissionsModule
import pl.llp.aircasting.util.Settings

class AircastingApplication : Application() {
    lateinit var appComponent: AppComponent
    lateinit var permissionsModule: PermissionsModule
    lateinit var databaseModule: DatabaseModule
    lateinit var mSettings: Settings

    override fun onCreate() {
        super.onCreate()

        DatabaseProvider.setup(applicationContext)

        mSettings = Settings(this)
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

    private fun setCorrectAppTheme() {
        if (mSettings.isDarkThemeEnabled()) AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_YES
        ) else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
