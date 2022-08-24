package pl.llp.aircasting.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.local.LogoutService
import pl.llp.aircasting.util.Settings
import javax.inject.Singleton

@Module
open class SettingsModule {
    @Provides
    @Singleton
    open fun providesSettings(application: AircastingApplication): Settings = application.settings

    @Provides
    @Singleton
    open fun providesLogoutService(settings: Settings, appContext: Context): LogoutService =
        LogoutService(settings, appContext)
}
