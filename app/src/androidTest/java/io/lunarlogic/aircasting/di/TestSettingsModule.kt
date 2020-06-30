package io.lunarlogic.aircasting.di

import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.lib.SettingsInterface
import org.mockito.Mockito

class TestSettingsModule: SettingsModule() {
    override fun providesSettings(application: AircastingApplication): SettingsInterface
            = Mockito.mock(Settings::class.java)
}