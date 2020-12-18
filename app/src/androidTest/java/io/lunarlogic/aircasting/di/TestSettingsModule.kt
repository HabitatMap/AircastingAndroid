package io.lunarlogic.aircasting.di

import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.di.mocks.FakeSettings
import io.lunarlogic.aircasting.lib.Settings

class TestSettingsModule: SettingsModule() {
    override fun providesSettings(application: AircastingApplication): Settings
            = FakeSettings(application)
}
