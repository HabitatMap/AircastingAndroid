package io.lunarlogic.aircasting

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import io.lunarlogic.aircasting.di.AppModule
import io.lunarlogic.aircasting.di.PermissionsModule
import io.lunarlogic.aircasting.models.observers.AppLifecycleObserver


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

    fun refreshComponent() {
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .permissionsModule(permissionsModule)
            .build()
    }
}
