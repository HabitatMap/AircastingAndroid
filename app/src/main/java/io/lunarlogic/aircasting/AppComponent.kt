package io.lunarlogic.aircasting

import dagger.Component
import io.lunarlogic.aircasting.di.*
import io.lunarlogic.aircasting.screens.create_account.CreateAccountActivity
import io.lunarlogic.aircasting.screens.dashboard.active.MobileActiveFragment
import io.lunarlogic.aircasting.screens.dashboard.dormant.MobileDormantFragment
import io.lunarlogic.aircasting.screens.dashboard.fixed.FixedFragment
import io.lunarlogic.aircasting.screens.dashboard.following.FollowingFragment
import io.lunarlogic.aircasting.screens.lets_start.LetsStartFragment
import io.lunarlogic.aircasting.screens.main.MainActivity
import io.lunarlogic.aircasting.screens.new_session.LoginActivity
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.screens.new_session.confirmation.ConfirmationFragment
import io.lunarlogic.aircasting.screens.new_session.session_details.SessionDetailsFragment
import io.lunarlogic.aircasting.screens.onboarding.OnboardingActivity
import io.lunarlogic.aircasting.screens.session_view.graph.GraphActivity
import io.lunarlogic.aircasting.screens.session_view.map.MapActivity
import io.lunarlogic.aircasting.screens.settings.SettingsFragment
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.ClearSDCardActivity
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.clearing_sd_card.ClearingSDCardFragment
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.my_account.MyAccountActivity
import io.lunarlogic.aircasting.screens.settings.clear_sd_card.sd_card_cleared.SDCardClearedFragment
import io.lunarlogic.aircasting.screens.sync.SyncActivity
import io.lunarlogic.aircasting.screens.sync.synced.AirbeamSyncedFragment
import io.lunarlogic.aircasting.screens.sync.syncing.AirbeamSyncingFragment
import io.lunarlogic.aircasting.sensor.AirBeamClearCardService
import io.lunarlogic.aircasting.sensor.AirBeamRecordSessionService
import io.lunarlogic.aircasting.sensor.AirBeamSyncService
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
    fun inject(activity: OnboardingActivity)
    fun inject(activity: LoginActivity)
    fun inject(activity: CreateAccountActivity)
    fun inject(activity: MainActivity)
    fun inject(activity: SyncActivity)
    fun inject(fragment: AirbeamSyncingFragment)
    fun inject(fragment: AirbeamSyncedFragment)
    fun inject(fragment: FollowingFragment)
    fun inject(fragment: MobileActiveFragment)
    fun inject(fragment: MobileDormantFragment)
    fun inject(fragment: ConfirmationFragment)
    fun inject(fragment: FixedFragment)
    fun inject(fragment: LetsStartFragment)
    fun inject(activity: NewSessionActivity)
    fun inject(fragment: SessionDetailsFragment)
    fun inject(activity: MapActivity)
    fun inject(activity: GraphActivity)

    fun inject(fragment: SettingsFragment)
    fun inject(activity: MyAccountActivity)
    fun inject(activity: ClearSDCardActivity)
    fun inject(fragment: ClearingSDCardFragment)
    fun inject(fragment: SDCardClearedFragment)

    fun inject(activity: MicrophoneService)
    fun inject(activity: AirBeamRecordSessionService)
    fun inject(activity: AirBeamSyncService)
    fun inject(activity: AirBeamClearCardService)
}
