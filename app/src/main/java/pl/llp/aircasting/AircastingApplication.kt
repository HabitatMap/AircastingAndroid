package pl.llp.aircasting

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.di.AppModule
import pl.llp.aircasting.di.PermissionsModule
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.data.model.observers.AppLifecycleObserver

class AircastingApplication: Application() {
    lateinit var appComponent: AppComponent
    lateinit var permissionsModule: PermissionsModule
    private var mSettings: Settings? = null

    override fun onCreate() {
        super.onCreate()

        DatabaseProvider.setup(applicationContext)

        mSettings = Settings(this)
        if (mSettings?.isThemeChangeEnabled() == true) AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_YES
        ) else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        permissionsModule = PermissionsModule()

        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .permissionsModule(permissionsModule)
            .build()
        appComponent.inject(this)

        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(AppLifecycleObserver())

    }
}
