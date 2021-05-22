package io.lunarlogic.aircasting.lib

import android.app.Application
import android.content.SharedPreferences
import io.lunarlogic.aircasting.networking.services.SessionsSyncService

open class Settings(mApplication: Application) {
    private val PRIVATE_MODE = 0
    protected val PREFERENCES_NAME = "preferences"
    protected val CROWD_MAP_ENABLED_KEY = "crowd_map"
    protected val CALIBRATION_KEY = "calibration"
    protected val MAPS_DISABLED_KEY = "maps_disabled"
    protected val BACKEND_URL_KEY = "backend_url"
    protected val BACKEND_PORT_KEY = "backend_port"
    protected val AIRBEAM3_CONNECTED_KEY = "airbeam3_connected" // this flag is used to check if airbeam3 was connected to the phone in the past
    protected val ONBOARDING_DISPLAYED_KEY = "onboarding_displayed"
    protected val APP_RESTARTED = "app_restarted"

    private val DELETE_SESSION_IN_PROGERSS_KEY = "delete_session_in_progress"
    private val SESSIONS_TO_REMOVE_KEY = "sessions_to_remove"

    private val DEFAULT_DELETE_SESSION_IN_PROGRESS = false
    private val DEFAULT_SESSIONS_TO_REMOVE = false
    private val DEFAULT_CALIBRATION_VALUE = 100
    private val DEFAULT_CROWD_MAP_ENABLED = true
    private val DEFAULT_MAPS_DISABLED = false
    protected open val DEFAULT_BACKEND_URL = "http://aircasting.org"
    protected val DEFAULT_BACKEND_PORT = "80"
    private val DEFAULT_AIRBEAM3_CONNECTED = false
    protected open val DEFAULT_ONBOARDING_DISPLAYED = false
    private val DEFAULT_APP_RESTARTED = false

    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = mApplication.getSharedPreferences(PREFERENCES_NAME, PRIVATE_MODE)
    }

    fun getCalibrationValue(): Int {
        return getIntFromSettings(CALIBRATION_KEY, DEFAULT_CALIBRATION_VALUE)
    }

    fun isCrowdMapEnabled(): Boolean {
        return getBooleanFromSettings(CROWD_MAP_ENABLED_KEY, DEFAULT_CROWD_MAP_ENABLED)
    }

    fun areMapsDisabled(): Boolean {
        return getBooleanFromSettings(MAPS_DISABLED_KEY, DEFAULT_MAPS_DISABLED)
    }

    fun airbeam3Connected(): Boolean {
        return getBooleanFromSettings(AIRBEAM3_CONNECTED_KEY, DEFAULT_AIRBEAM3_CONNECTED)
    }

    fun appRestarted(): Boolean {
        return getBooleanFromSettings(APP_RESTARTED, DEFAULT_APP_RESTARTED)
    }

    open fun onboardingDisplayed(): Boolean {
        return getBooleanFromSettings(ONBOARDING_DISPLAYED_KEY, DEFAULT_ONBOARDING_DISPLAYED)
    }

    open fun getBackendUrl(): String? {
        return getStringFromSettings(BACKEND_URL_KEY, DEFAULT_BACKEND_URL)
    }

    open fun getBackendPort(): String? {
        return getStringFromSettings(BACKEND_PORT_KEY, DEFAULT_BACKEND_PORT)
    }

    fun getIsDeleteSessionInProgress(): Boolean? {
        return getBooleanFromSettings(DELETE_SESSION_IN_PROGERSS_KEY, DEFAULT_DELETE_SESSION_IN_PROGRESS)
    }

    fun getAreThereSessionsToRemove(): Boolean? {
        return getBooleanFromSettings(SESSIONS_TO_REMOVE_KEY, DEFAULT_SESSIONS_TO_REMOVE)
    }

    fun toggleMapSettingsEnabled(){
        val enabled = !areMapsDisabled()
        saveToSettings(MAPS_DISABLED_KEY, enabled)
    }

    fun toggleCrowdMapEnabled() {
        val enabled = !isCrowdMapEnabled()
        saveToSettings(CROWD_MAP_ENABLED_KEY, enabled)
    }

    fun setAirbeam3Connected() {
        saveToSettings(AIRBEAM3_CONNECTED_KEY, true)
    }

    open fun onboardingAccepted() {
        saveToSettings(ONBOARDING_DISPLAYED_KEY, true)
    }

    open fun onboardingNotDisplayed() {
        saveToSettings(ONBOARDING_DISPLAYED_KEY, false)
    }

    fun microphoneSettingsChanged(calibration: Int){
        saveToSettings(CALIBRATION_KEY, calibration)
    }

    fun backendSettingsChanged(url: String, port: String) {
        saveToSettings(BACKEND_URL_KEY, url)
        saveToSettings(BACKEND_PORT_KEY, port)
        SessionsSyncService.destroy()
    }

    fun setAppRestarted() {
        saveToSettings(APP_RESTARTED, true)
    }

    fun setAppNotRestarted() {
        saveToSettings(APP_RESTARTED, false)
    }

    fun setSessionsToRemove(toRemove: Boolean) {
        saveToSettings(SESSIONS_TO_REMOVE_KEY, toRemove)
    }

    fun setDeletingSessionsInProgress(deleteInProgress: Boolean) {
        saveToSettings(DELETE_SESSION_IN_PROGERSS_KEY, deleteInProgress)
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

    open fun getIntFromSettings(key: String, default: Int): Int {
        return sharedPreferences.getInt(key, default)
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

    protected open fun saveToSettings(key: String, value: Int){
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.commit()
    }

    private fun deleteFromSettings(){
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
