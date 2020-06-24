package io.lunarlogic.aircasting

import dagger.Component
import io.lunarlogic.aircasting.di.AppModule
import io.lunarlogic.aircasting.di.PermissionsModule
import io.lunarlogic.aircasting.di.SensorsModule
import io.lunarlogic.aircasting.di.SettingsModule
import io.lunarlogic.aircasting.screens.dashboard.fixed.FixedFragment
import io.lunarlogic.aircasting.screens.dashboard.mobile.MobileActiveFragment
import io.lunarlogic.aircasting.screens.dashboard.mobile.MobileDormantFragment
import io.lunarlogic.aircasting.screens.main.MainActivity
import io.lunarlogic.aircasting.screens.new_session.LoginActivity
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        SettingsModule::class,
        PermissionsModule::class,
        SensorsModule::class
    ]
)
interface AppComponent {
    fun inject(app: AircastingApplication)
    fun inject(activity: LoginActivity)
    fun inject(activity: MainActivity)
    fun inject(fragment: MobileActiveFragment)
    fun inject(fragment: MobileDormantFragment)
    fun inject(fragment: FixedFragment)
    fun inject(activity: NewSessionActivity)
}