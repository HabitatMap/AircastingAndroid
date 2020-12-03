package io.lunarlogic.aircasting.lib

import android.app.Application
import android.content.SharedPreferences

open class Settings(mApplication: Application) {
    private val PRIVATE_MODE = 0
    protected val PREFERENCES_NAME = "preferences"
    protected val EMAIL_KEY = "email"
    protected val AUTH_TOKEN_KEY = "auth_token"
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = mApplication.getSharedPreferences(PREFERENCES_NAME, PRIVATE_MODE)
    }

    fun getAuthToken(): String? {
        return getFromSettings(AUTH_TOKEN_KEY)
    }

    fun getEmail(): String? {
        return getFromSettings(EMAIL_KEY)
    }

    fun login(email: String, authToken: String) {
        saveToSettings(EMAIL_KEY, email)
        saveToSettings(AUTH_TOKEN_KEY, authToken)
    }

    open fun getFromSettings(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    open protected fun saveToSettings(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.commit()
    }

    open fun logout(){
        deleteFromSettings()
    }

    private fun deleteFromSettings(){
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
