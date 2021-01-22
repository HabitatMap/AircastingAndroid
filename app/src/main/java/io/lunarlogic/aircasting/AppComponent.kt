package io.lunarlogic.aircasting

import dagger.Component
import io.lunarlogic.aircasting.di.*
import io.lunarlogic.aircasting.screens.create_account.CreateAccountActivity
import io.lunarlogic.aircasting.screens.dashboard.fixed.FixedFragment
import io.lunarlogic.aircasting.screens.dashboard.following.FollowingFragment
import io.lunarlogic.aircasting.screens.dashboard.active.MobileActiveFragment
import io.lunarlogic.aircasting.screens.dashboard.dormant.MobileDormantFragment
import io.lunarlogic.aircasting.screens.main.MainActivity
import io.lunarlogic.aircasting.screens.new_session.LoginActivity
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.screens.new_session.confirmation.ConfirmationFragment
import io.lunarlogic.aircasting.screens.new_session.session_details.SessionDetailsFragment
import io.lunarlogic.aircasting.screens.settings.SettingsFragment
import io.lunarlogic.aircasting.screens.settings.myaccount.MyAccountActivity
import io.lunarlogic.aircasting.sensor.AirbeamService
import io.lunarlogic.aircasting.sensor.microphone.MicrophoneService
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        ApiModule::class,
        SettingsModule::class,
        PermissionsModule::class,
        SensorsModule::class,
        NewSessionWizardModule::class
    ]
)
interface AppComponent {
    fun inject(app: AircastingApplication)
    fun inject(activity: LoginActivity)
    fun inject(activity: CreateAccountActivity)
    fun inject(activity: MainActivity)
    fun inject(fragment: FollowingFragment)
    fun inject(fragment: MobileActiveFragment)
    fun inject(fragment: MobileDormantFragment)
    fun inject(fragment: ConfirmationFragment)
    fun inject(fragment: FixedFragment)
    fun inject(activity: NewSessionActivity)
    fun inject(fragment: SessionDetailsFragment)

    fun inject(fragment: SettingsFragment)
    fun inject(activity: MyAccountActivity)

    fun inject(activity: MicrophoneService)
    fun inject(activity: AirbeamService)
}
