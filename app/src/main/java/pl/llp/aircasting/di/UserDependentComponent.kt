package pl.llp.aircasting.di

import dagger.Subcomponent
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.di.modules.ApiModule
import pl.llp.aircasting.di.modules.CoroutineModule
import pl.llp.aircasting.di.modules.DevicesModule
import pl.llp.aircasting.di.modules.FlowModule
import pl.llp.aircasting.di.modules.FragmentModule
import pl.llp.aircasting.di.modules.HelperModule
import pl.llp.aircasting.di.modules.NewSessionWizardModule
import pl.llp.aircasting.di.modules.PermissionsModule
import pl.llp.aircasting.di.modules.RepositoryModule
import pl.llp.aircasting.di.modules.SyncModule
import pl.llp.aircasting.di.modules.ViewModelModule
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.ui.view.fragments.AirbeamSyncedFragment
import pl.llp.aircasting.ui.view.fragments.AirbeamSyncingFragment
import pl.llp.aircasting.ui.view.fragments.ClearingSDCardFragment
import pl.llp.aircasting.ui.view.fragments.DashboardFragment
import pl.llp.aircasting.ui.view.fragments.FixedFragment
import pl.llp.aircasting.ui.view.fragments.FollowingFragment
import pl.llp.aircasting.ui.view.fragments.LetsBeginFragment
import pl.llp.aircasting.ui.view.fragments.MobileActiveFragment
import pl.llp.aircasting.ui.view.fragments.MobileDormantFragment
import pl.llp.aircasting.ui.view.fragments.ReorderingDashboardFragment
import pl.llp.aircasting.ui.view.fragments.ReorderingFollowingFragment
import pl.llp.aircasting.ui.view.fragments.SDCardClearedFragment
import pl.llp.aircasting.ui.view.fragments.SettingsFragment
import pl.llp.aircasting.ui.view.fragments.search_follow_fixed_session.SearchLocationFragment
import pl.llp.aircasting.ui.view.fragments.search_follow_fixed_session.SearchLocationResultFragment
import pl.llp.aircasting.ui.view.screens.create_account.CreateAccountActivity
import pl.llp.aircasting.ui.view.screens.dashboard.active.AddNoteBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.active.DisconnectedView
import pl.llp.aircasting.ui.view.screens.dashboard.active.FinishSessionConfirmationDialog
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.edit.EditSessionBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.share.ShareSessionBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.theshold_alerts.CreateThresholdAlertBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.mobile.active.MobileActiveSessionActionsBottomSheet
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
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.reader.SyncableAirBeamReader
import pl.llp.aircasting.util.helpers.sensor.microphone.MicrophoneService
import pl.llp.aircasting.util.helpers.sensor.services.AirBeamClearCardService
import pl.llp.aircasting.util.helpers.sensor.services.AirBeamReconnectSessionService
import pl.llp.aircasting.util.helpers.sensor.services.AirBeamRecordSessionService
import pl.llp.aircasting.util.helpers.sensor.services.AirBeamSyncService
import pl.llp.aircasting.util.helpers.sensor.services.BatteryLevelService
import javax.inject.Scope

@UserSessionScope
@Subcomponent(
    modules = [
        ApiModule::class,
        PermissionsModule::class,
        DevicesModule::class,
        NewSessionWizardModule::class,
        ViewModelModule::class,
        CoroutineModule::class,
        FragmentModule::class,
        RepositoryModule::class,
        SyncModule::class,
        FlowModule::class,
        HelperModule::class,
    ]
)
interface UserDependentComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): UserDependentComponent
    }
    fun inject(target: SyncableAirBeamReader)
    fun inject(target: BatteryLevelService)

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

    fun inject(bottomSheet: CreateThresholdAlertBottomSheet)
    fun inject(bottomSheet: EditSessionBottomSheet)
    fun inject(bottomSheet: ShareSessionBottomSheet)
    fun inject(bottomSheet: MobileActiveSessionActionsBottomSheet)
    fun inject(bottomSheet: AddNoteBottomSheet)

    fun inject(dialog: FinishSessionConfirmationDialog)
    fun inject(view: DisconnectedView)

    fun inject(fragment: SettingsFragment)
    fun inject(activity: MyAccountActivity)
    fun inject(activity: ClearSDCardActivity)
    fun inject(fragment: ClearingSDCardFragment)
    fun inject(fragment: SDCardClearedFragment)
    fun inject(fragment: SearchLocationFragment)
    fun inject(fragment: SearchLocationResultFragment)

    fun inject(activity: MicrophoneService)
    fun inject(activity: AirBeamRecordSessionService)
    fun inject(activity: AirBeamSyncService)
    fun inject(activity: AirBeamClearCardService)
    fun inject(activity: AirBeamReconnectSessionService)
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class UserSessionScope