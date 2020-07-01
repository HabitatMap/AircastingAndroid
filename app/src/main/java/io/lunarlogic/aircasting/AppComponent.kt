package io.lunarlogic.aircasting

import dagger.Component
import io.lunarlogic.aircasting.di.AppModule
import io.lunarlogic.aircasting.di.SettingsModule
import io.lunarlogic.aircasting.screens.dashboard.MobileActiveFragment
import io.lunarlogic.aircasting.screens.dashboard.MobileDormantFragment
import io.lunarlogic.aircasting.screens.main.MainActivity
import io.lunarlogic.aircasting.screens.new_session.LoginActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, SettingsModule::class])
interface AppComponent {
    fun inject(app: AircastingApplication)
    fun inject(activity: MainActivity)
    fun inject(activity: LoginActivity)
    fun inject(fragment: MobileActiveFragment)
    fun inject(fragment: MobileDormantFragment)
}