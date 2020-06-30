package io.lunarlogic.aircasting.di

import dagger.Module
import dagger.Provides
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.lib.SettingsInterface

@Module
class AppModule {
    @Provides
    fun providesSettings(): SettingsInterface = Settings()
}