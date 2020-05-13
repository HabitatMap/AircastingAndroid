package io.lunarlogic.aircasting.lib

import android.content.Context
import android.content.SharedPreferences

class Settings(val mContext: Context) {
    private val PRIVATE_MODE = 0
    private val PREFERENCES_NAME = "auth_token"
    private val AUTH_TOKEN_KEY = "auth_token"
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = mContext.getSharedPreferences(PREFERENCES_NAME, PRIVATE_MODE)
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString(AUTH_TOKEN_KEY, null)
    }

    fun setAuthToken(authToken: String) {
        val editor = sharedPreferences.edit()
        editor.putString(AUTH_TOKEN_KEY, authToken)
        editor.commit()
    }
}