package io.lunarlogic.aircasting.di

import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.Settings

class FakeSettings(application: AircastingApplication): Settings(application) {
//    private var mEmail: String? = null
//    private var mToken: String? = null
    private val PREFERENCES_NAME = "auth_token"
    private val EMAIL_KEY = "email"
    private val AUTH_TOKEN_KEY = "auth_token"

    private var preferences : HashMap<String, String> = HashMap()

    override fun getAuthToken(): String? {
        return preferences.get(AUTH_TOKEN_KEY)
    }

    override fun getEmail(): String? {
        return preferences.get(EMAIL_KEY)
    }

    override fun login(email: String, authToken: String) {
//        this.mEmail = email
//        this.mToken = authToken
        this.preferences.put(EMAIL_KEY, email)
        this.preferences.put(AUTH_TOKEN_KEY, authToken)
    }

    override fun logout(){
        preferences.clear()
    }


}

class TestSettingsModule: SettingsModule() {
    override fun providesSettings(application: AircastingApplication): Settings
            = FakeSettings(application)
}
