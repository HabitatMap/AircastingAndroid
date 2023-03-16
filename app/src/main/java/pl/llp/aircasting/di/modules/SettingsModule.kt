package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.util.Settings
import javax.inject.Singleton

@Module
open class SettingsModule {
    @Provides
    @Singleton
    open fun providesSettings(application: AircastingApplication): Settings = application.settings
}
