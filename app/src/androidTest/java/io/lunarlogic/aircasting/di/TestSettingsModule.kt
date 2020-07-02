package io.lunarlogic.aircasting.di

import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.Settings

class FakeSettings(application: AircastingApplication): Settings(application) {
    private var token: String? = null

    override fun getAuthToken(): String? {
        return token
    }

    override fun setAuthToken(authToken: String) {
        token = authToken
    }
}

class TestSettingsModule: SettingsModule() {
    override fun providesSettings(application: AircastingApplication): Settings
            = FakeSettings(application)
}