package io.lunarlogic.aircasting.lib

import android.app.Application
import android.content.SharedPreferences
import android.util.Log

open class Settings(mApplication: Application) {
    private val PRIVATE_MODE = 0
    protected val PREFERENCES_NAME = "preferences"
    protected val EMAIL_KEY = "email"
    protected val AUTH_TOKEN_KEY = "auth_token"
    protected val CROWD_MAP_ENABLED_KEY = "crowd_map"
    protected val BACKEND_URL_KEY = "backend_url"
    protected val BACKEND_PORT_KEY = "backend_port"

    private val DEFAULT_CROWD_MAP_ENABLED = true
    protected open val DEFAULT_BACKEND_URL = "http://aircasting.org"
    protected val DEFAULT_BACKEND_PORT = "80"

    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = mApplication.getSharedPreferences(PREFERENCES_NAME, PRIVATE_MODE)
    }

    fun getAuthToken(): String? {
        return getStringFromSettings(AUTH_TOKEN_KEY)
    }

    fun getEmail(): String? {
        return getStringFromSettings(EMAIL_KEY)
    }

    fun toggleCrowdMapEnabled() {
        val enabled = !isCrowdMapEnabled()
        saveToSettings(CROWD_MAP_ENABLED_KEY, enabled)
        Log.i("SETTINGS", enabled.toString())
    }

    fun isCrowdMapEnabled(): Boolean {
        return getBooleanFromSettings(CROWD_MAP_ENABLED_KEY, DEFAULT_CROWD_MAP_ENABLED)
    }

    open fun getBackendUrl(): String? {
        return getStringFromSettings(BACKEND_URL_KEY, DEFAULT_BACKEND_URL)
    }

    open fun getBackendPort(): String? {
        return getStringFromSettings(BACKEND_PORT_KEY, DEFAULT_BACKEND_PORT)
    }

    fun backendSettingsChanged(url: String, port: String) {
        saveToSettings(BACKEND_URL_KEY, url)
        saveToSettings(BACKEND_PORT_KEY, port)
    }

    fun login(email: String, authToken: String) {
        saveToSettings(EMAIL_KEY, email)
        saveToSettings(AUTH_TOKEN_KEY, authToken)
    }

    open fun logout(){
        deleteFromSettings()
    }

    open fun getStringFromSettings(key: String, default: String? = null): String? {
        return sharedPreferences.getString(key, default)
    }

    open fun getBooleanFromSettings(key: String, default: Boolean = true): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    protected open fun saveToSettings(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.commit()
    }

    protected open fun saveToSettings(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.commit()
    }

    private fun deleteFromSettings(){
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
