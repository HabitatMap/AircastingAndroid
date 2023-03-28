package pl.llp.aircasting.di.components

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.api.services.Authenticated
import pl.llp.aircasting.di.modules.SensorsModule
import pl.llp.aircasting.ui.view.fragments.*
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.share.ShareSessionBottomSheet
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.ui.view.screens.settings.my_account.MyAccountActivity
import pl.llp.aircasting.ui.view.screens.sync.SyncActivity
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.helpers.sensor.AirBeamSyncService
import javax.inject.Scope

@UserSessionScope
@Subcomponent(
    modules = [
        UserApiModule::class,
        SensorsModule::class,
    ]
)
interface UserComponent {

    fun inject(it: MainActivity)
    fun inject(it: MyAccountActivity)
    fun inject(it: MobileActiveFragment)
    fun inject(it: ReorderingFollowingFragment)
    fun inject(it: DashboardFragment)
    fun inject(it: FixedFragment)
    fun inject(it: FollowingFragment)
    fun inject(it: MobileDormantFragment)
    fun inject(it: SettingsFragment)
    fun inject(it: ShareSessionBottomSheet)
    fun inject(it: SyncActivity)
    fun inject(it: AirBeamSyncService)

    @Subcomponent.Factory
    interface Factory {
        fun create(): UserComponent
    }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class UserSessionScope

@Module
class UserApiModule {
    @Provides
    @UserSessionScope
    @Authenticated
    fun providesApiServiceAuthenticatedWithToken(
        settings: Settings,
        factory: ApiServiceFactory
    ): ApiService {
        return factory.getAuthenticated(settings.getAuthToken())
    }
}