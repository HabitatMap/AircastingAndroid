package io.lunarlogic.aircasting.di

import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.Settings
import org.mockito.Mockito

class TestSettingsModuleMocked: SettingsModule() {
    override fun providesSettings(application: AircastingApplication): Settings
            = Mockito.mock(Settings::class.java)
}