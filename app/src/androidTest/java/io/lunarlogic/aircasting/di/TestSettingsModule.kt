package io.lunarlogic.aircasting.di

import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.SettingsInterface

class TestSettingsModule: SettingsModule() {
    override fun providesSettings(application: AircastingApplication): SettingsInterface = FakeSettings(application)
}