package pl.llp.aircasting

import dagger.Component
import pl.llp.aircasting.di.*
import pl.llp.aircasting.ui.view.screens.common.BaseActivity
import pl.llp.aircasting.ui.view.screens.create_account.CreateAccountActivity
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardFragment
import pl.llp.aircasting.ui.view.screens.dashboard.active.MobileActiveFragment
import pl.llp.aircasting.ui.view.screens.dashboard.dormant.MobileDormantFragment
import pl.llp.aircasting.ui.view.screens.dashboard.fixed.FixedFragment
import pl.llp.aircasting.ui.view.screens.dashboard.following.FollowingFragment
import pl.llp.aircasting.ui.view.screens.dashboard.reordering_dashboard.ReorderingDashboardFragment
import pl.llp.aircasting.ui.view.screens.dashboard.reordering_following.ReorderingFollowingFragment
import pl.llp.aircasting.ui.view.screens.lets_begin.LetsBeginFragment
import pl.llp.aircasting.ui.view.screens.login.LoginActivity
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.ui.view.screens.new_session.NewSessionActivity
import pl.llp.aircasting.ui.view.screens.new_session.confirmation.ConfirmationFragment
import pl.llp.aircasting.ui.view.screens.new_session.session_details.SessionDetailsFragment
import pl.llp.aircasting.ui.view.screens.onboarding.OnboardingActivity
import pl.llp.aircasting.ui.view.screens.search.SearchFixedResultActivity
import pl.llp.aircasting.ui.view.screens.search.SearchFixedSessionsActivity
import pl.llp.aircasting.ui.view.screens.session_view.graph.GraphActivity
import pl.llp.aircasting.ui.view.screens.session_view.map.MapActivity
import pl.llp.aircasting.ui.view.screens.settings.SettingsFragment
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.ClearSDCardActivity
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.clearing_sd_card.ClearingSDCardFragment
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.sd_card_cleared.SDCardClearedFragment
import pl.llp.aircasting.ui.view.screens.settings.my_account.MyAccountActivity
import pl.llp.aircasting.ui.view.screens.sync.SyncActivity
import pl.llp.aircasting.ui.view.screens.sync.synced.AirbeamSyncedFragment
import pl.llp.aircasting.ui.view.screens.sync.syncing.AirbeamSyncingFragment
import pl.llp.aircasting.util.helpers.sensor.AirBeamClearCardService
import pl.llp.aircasting.util.helpers.sensor.AirBeamRecordSessionService
import pl.llp.aircasting.util.helpers.sensor.AirBeamSyncService
import pl.llp.aircasting.util.helpers.sensor.microphone.MicrophoneService
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        ApiModule::class,
        NetworkModule::class,
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

    fun inject(activity: SearchFixedSessionsActivity)
    fun inject(activity: SearchFixedResultActivity)

    fun inject(fragment: AirbeamSyncingFragment)
    fun inject(fragment: AirbeamSyncedFragment)
    fun inject(fragment: FollowingFragment)
    fun inject(fragment: MobileActiveFragment)
    fun inject(fragment: MobileDormantFragment)
    fun inject(fragment: ConfirmationFragment)
    fun inject(fragment: FixedFragment)
    fun inject(fragment: LetsBeginFragment)
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
