package io.lunarlogic.aircasting.lib

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

interface SettingsInterface {
    fun getAuthToken(): String?
}

open class Settings(application: Application): SettingsInterface {
    override fun getAuthToken(): String? {
        return "real auth token"
    }
}

//class Settings(val mContext: Context) {
//    private val PRIVATE_MODE = 0
//    private val PREFERENCES_NAME = "settings"
//    private val AUTH_TOKEN_KEY = "auth_token"
//    private val EMAIL_KEY = "email"
//    private val sharedPreferences: SharedPreferences
//
//    init {
//        sharedPreferences = mContext.getSharedPreferences(PREFERENCES_NAME, PRIVATE_MODE)
//    }
//
//    fun getEmail(): String? {
//        return sharedPreferences.getString(EMAIL_KEY, null)
//    }
//
//    fun getAuthToken(): String? {
//        return sharedPreferences.getString(AUTH_TOKEN_KEY, null)
//    }
//
//    fun setEmail(email: String) {
//        saveToSettings(EMAIL_KEY, email)
//    }
//
//    fun setAuthToken(authToken: String) {
//        saveToSettings(AUTH_TOKEN_KEY, authToken)
//    }
//
//    private fun saveToSettings(key: String, value: String) {
//        val editor = sharedPreferences.edit()
//        editor.putString(key, value)
//        editor.commit()
//    }
//}

