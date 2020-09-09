package io.lunarlogic.aircasting.di

import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.Settings

class FakeSettings(application: AircastingApplication): Settings(application) {
    private var mEmail: String? = null
    private var mToken: String? = null

    override fun getAuthToken(): String? {
        return mToken
    }

    override fun login(email: String, authToken: String) {
        this.mEmail = email
        this.mToken = authToken
    }
}

class TestSettingsModule: SettingsModule() {
    override fun providesSettings(application: AircastingApplication): Settings
            = FakeSettings(application)
}
