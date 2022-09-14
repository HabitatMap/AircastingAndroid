package pl.llp.aircasting.di.mocks

import android.content.SharedPreferences
import pl.llp.aircasting.util.Settings

class FakeSettings(preferences: SharedPreferences): Settings(preferences) {
    private var preferences : HashMap<String, String> = HashMap()
    val DEFAULT_BACKEND_URL = "/"
    val BACKEND_URL_KEY = "backend_url"

    override fun getStringFromSettings(key: String, default: String?): String? {
        return preferences.get(key)
    }

    override fun getBackendUrl(): String? {
        return getStringFromSettings(BACKEND_URL_KEY, DEFAULT_BACKEND_URL)
    }

    override fun saveToSettings(key: String, value: String) {
        this.preferences.put(key, value)
    }

    override fun logout(){
        preferences.clear()
    }
}
