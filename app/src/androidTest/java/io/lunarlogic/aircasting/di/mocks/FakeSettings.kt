package io.lunarlogic.aircasting.di.mocks

import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.Settings

class FakeSettings(application: AircastingApplication): Settings(application) {
    private var preferences : HashMap<String, String> = HashMap()
    override val DEFAULT_BACKEND_URL = "/"

    override fun getStringFromSettings(key: String, default: String?): String? {
        return preferences.get(key)
    }


    override fun saveToSettings(key: String, value: String) {
        this.preferences.put(key, value)
    }

    override fun logout(){
        preferences.clear()
    }
}
