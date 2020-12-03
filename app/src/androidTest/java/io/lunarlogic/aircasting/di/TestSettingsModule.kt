package io.lunarlogic.aircasting.di

import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.Settings

class FakeSettings(application: AircastingApplication): Settings(application) {
    private var preferences : HashMap<String, String> = HashMap()

    override fun getFromSettings(key: String): String? {
        return preferences.get(key)
    }

    override fun saveToSettings(key: String, value: String) {
        this.preferences.put(key, value)
    }

    override fun logout(){
        preferences.clear()
    }
}

class TestSettingsModule: SettingsModule() {
    override fun providesSettings(application: AircastingApplication): Settings
            = FakeSettings(application)
}
