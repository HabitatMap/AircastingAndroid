package pl.llp.aircasting.di

import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.di.mocks.FakeSettings
import pl.llp.aircasting.lib.Settings

class TestSettingsModule: SettingsModule() {
    override fun providesSettings(application: AircastingApplication): Settings
            = FakeSettings(application)
}
