package pl.llp.aircasting

import dagger.Component
import pl.llp.aircasting.di.*
import pl.llp.aircasting.screens.common.BaseActivity
import pl.llp.aircasting.screens.create_account.CreateAccountActivity
import pl.llp.aircasting.screens.dashboard.DashboardFragment
import pl.llp.aircasting.screens.dashboard.active.MobileActiveFragment
import pl.llp.aircasting.screens.dashboard.dormant.MobileDormantFragment
import pl.llp.aircasting.screens.dashboard.fixed.FixedFragment
import pl.llp.aircasting.screens.dashboard.following.FollowingFragment
import pl.llp.aircasting.screens.dashboard.reordering_dashboard.ReorderingDashboardFragment
import pl.llp.aircasting.screens.dashboard.reordering_following.ReorderingFollowingFragment
import pl.llp.aircasting.screens.lets_begin.LetsStartFragment
import pl.llp.aircasting.screens.login.LoginActivity
import pl.llp.aircasting.screens.main.MainActivity
import pl.llp.aircasting.screens.new_session.NewSessionActivity
import pl.llp.aircasting.screens.new_session.confirmation.ConfirmationFragment
import pl.llp.aircasting.screens.new_session.session_details.SessionDetailsFragment
import pl.llp.aircasting.screens.onboarding.OnboardingActivity
import pl.llp.aircasting.screens.session_view.graph.GraphActivity
import pl.llp.aircasting.screens.session_view.map.MapActivity
import pl.llp.aircasting.screens.settings.SettingsFragment
import pl.llp.aircasting.screens.settings.clear_sd_card.ClearSDCardActivity
import pl.llp.aircasting.screens.settings.clear_sd_card.clearing_sd_card.ClearingSDCardFragment
import pl.llp.aircasting.screens.settings.clear_sd_card.sd_card_cleared.SDCardClearedFragment
import pl.llp.aircasting.screens.settings.my_account.MyAccountActivity
import pl.llp.aircasting.screens.sync.SyncActivity
import pl.llp.aircasting.screens.sync.synced.AirbeamSyncedFragment
import pl.llp.aircasting.screens.sync.syncing.AirbeamSyncingFragment
import pl.llp.aircasting.sensor.AirBeamClearCardService
import pl.llp.aircasting.sensor.AirBeamRecordSessionService
import pl.llp.aircasting.sensor.AirBeamSyncService
import pl.llp.aircasting.sensor.microphone.MicrophoneService
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
    fun inject(activity: BaseActivity)
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
    fun inject(fragment: ReorderingFollowingFragment)
    fun inject(fragment: DashboardFragment)
    fun inject(fragment: ReorderingDashboardFragment)

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
