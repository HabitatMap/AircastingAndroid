package io.lunarlogic.aircasting

import android.app.Application
import io.lunarlogic.aircasting.di.AppModule

class AircastingApplication: Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
        appComponent.inject(this)
    }
}