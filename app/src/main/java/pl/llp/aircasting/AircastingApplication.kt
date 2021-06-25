package pl.llp.aircasting

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import pl.llp.aircasting.di.AppModule
import pl.llp.aircasting.di.PermissionsModule
import pl.llp.aircasting.models.observers.AppLifecycleObserver


class AircastingApplication: Application() {
    lateinit var appComponent: AppComponent
    lateinit var permissionsModule: PermissionsModule

    override fun onCreate() {
        super.onCreate()
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
