package pl.llp.aircasting.di

import dagger.Component
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.di.modules.*
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.ui.view.fragments.*
import pl.llp.aircasting.ui.view.fragments.search_follow_fixed_session.SearchLocationFragment
import pl.llp.aircasting.ui.view.screens.create_account.CreateAccountActivity
import pl.llp.aircasting.ui.view.screens.login.LoginActivity
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.ui.view.screens.new_session.NewSessionActivity
import pl.llp.aircasting.ui.view.screens.new_session.confirmation.ConfirmationFragment
import pl.llp.aircasting.ui.view.screens.new_session.session_details.SessionDetailsFragment
import pl.llp.aircasting.ui.view.screens.onboarding.OnboardingActivity
import pl.llp.aircasting.ui.view.screens.search.SearchFixedSessionActivity
import pl.llp.aircasting.ui.view.screens.session_view.graph.GraphActivity
import pl.llp.aircasting.ui.view.screens.session_view.map.MapActivity
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.ClearSDCardActivity
import pl.llp.aircasting.ui.view.screens.settings.my_account.MyAccountActivity
import pl.llp.aircasting.ui.view.screens.sync.SyncActivity
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
        DatabaseModule::class,
        NetworkModule::class,
        SettingsModule::class,
        PermissionsModule::class,
        SensorsModule::class,
        NewSessionWizardModule::class,
        ViewModelModule::class,
        DispatcherModule::class,
        FragmentModule::class
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

    fun inject(activity: SearchFixedSessionActivity)
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
    fun inject(fragment: SearchLocationFragment)

    fun inject(activity: MicrophoneService)
    fun inject(activity: AirBeamRecordSessionService)
    fun inject(activity: AirBeamSyncService)
    fun inject(activity: AirBeamClearCardService)
}
