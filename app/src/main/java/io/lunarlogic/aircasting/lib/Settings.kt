package io.lunarlogic.aircasting.lib

import android.app.Application
import android.content.SharedPreferences
import android.util.Log

open class Settings(mApplication: Application) {
    private val PRIVATE_MODE = 0
    protected val PREFERENCES_NAME = "preferences"
    protected val EMAIL_KEY = "email"
    protected val AUTH_TOKEN_KEY = "auth_token"
    protected val CROWD_MAP_SETTING = "crowd_map_setting"
    protected val ADRESS_BACKEND_SETTING = "adress_backend_setting"
    protected val PORT_BACKEND_SETTING = "port_backend_setting"

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

    fun crowdMapSettingSwitched(){
        val check = sharedPreferences.getBoolean(CROWD_MAP_SETTING, false) //todo: is this false for sure? got from stackoverflow
        val editor = sharedPreferences.edit()
        editor.putBoolean(CROWD_MAP_SETTING, !check)
        editor.apply()
        Log.i("SETTINGS", check.toString())
    }

    fun crowdMapSettingInit(){
        //TODO: where this method should be used ??
        val editor = sharedPreferences.edit()
        editor.putBoolean(CROWD_MAP_SETTING, true)
        editor.apply()
    }

    fun backendSettingsChanged(adress: String, port: String){
        val editor = sharedPreferences.edit()
        editor.putString(ADRESS_BACKEND_SETTING, adress)
        editor.putString(PORT_BACKEND_SETTING, port)
        editor.apply()
    }

    fun backendSettingsInit(){
        //TODO: not sure if its needed and why should it be used
        val editor = sharedPreferences.edit()
//        editor.putString(ADRESS_BACKEND_SETTING, adress)
//        editor.putString(PORT_BACKEND_SETTING, adress)
        editor.apply()
    }
}
