package io.lunarlogic.aircasting.di

import dagger.Module
import dagger.Provides
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.Settings
import javax.inject.Singleton

@Module
open class SettingsModule {
    @Provides
    @Singleton
    open fun providesSettings(application: AircastingApplication): Settings = Settings(application)
}
