package pl.llp.aircasting.di

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.lib.Settings
import javax.inject.Singleton

@Module
open class SettingsModule {
    @Provides
    @Singleton
    open fun providesSettings(application: AircastingApplication): Settings = Settings(application)
}
