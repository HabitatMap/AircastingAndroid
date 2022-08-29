package pl.llp.aircasting.di

import android.app.Application
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.di.mocks.FakeSettings
import pl.llp.aircasting.di.modules.SettingsModule
import pl.llp.aircasting.util.Settings

class TestSettingsModule: SettingsModule() {
    override fun providesSettings(application: AircastingApplication): Settings
            = FakeSettings(application.getSharedPreferences(
        Settings.PREFERENCES_NAME,
        Application.MODE_PRIVATE
    ))
}
