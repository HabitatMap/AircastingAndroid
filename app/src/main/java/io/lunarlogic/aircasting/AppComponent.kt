package io.lunarlogic.aircasting

import dagger.Component
import io.lunarlogic.aircasting.di.AppModule
import io.lunarlogic.aircasting.screens.main.MainActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)
}