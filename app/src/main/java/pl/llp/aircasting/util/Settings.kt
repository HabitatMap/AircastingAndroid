package pl.llp.aircasting.util

import android.app.Application
import android.content.SharedPreferences
import com.jakewharton.processphoenix.ProcessPhoenix
import pl.llp.aircasting.data.local.LogoutService

open class Settings(private val mApplication: Application) {
    private val PRIVATE_MODE = 0
    private val PREFERENCES_NAME = "preferences"
    private val PROFILE_NAME_KEY = "profile_name"
    private val EMAIL_KEY = "email"
    private val AUTH_TOKEN_KEY = "auth_token"
    private val USE_24_HOUR_FORMAT_KEY = "use_24_hour_format"
    private val USE_CELSIUS_SCALE_KEY = "use_celsius_scale"
    private val CROWD_MAP_ENABLED_KEY = "crowd_map"
    private val CALIBRATION_KEY = "calibration"
    private val MAPS_DISABLED_KEY = "maps_disabled"
    private val BACKEND_URL_KEY = "backend_url"
    private val BACKEND_PORT_KEY = "backend_port"
    private val ONBOARDING_DISPLAYED_KEY = "onboarding_displayed"
    private val APP_RESTARTED = "app_restarted"
    private val FOLLOWED_SESSIONS_NUMBER_KEY = "followed_sesions_number"
    private val MOBILE_ACTIVE_NUMBERS_KEY = "mobile_active_sessions"
    private val THEME_SET_TO_DARK_KEY = "theme_dark"
    private val KEEP_SCREEN_ON_KEY = "keep_screen_on"
    private val USE_SATELLITE_VIEW = "use_satellite_view"

    private val DELETE_SESSION_IN_PROGERSS_KEY = "delete_session_in_progress"
    private val SESSIONS_TO_REMOVE_KEY = "sessions_to_remove"
    private val EXPANDED_SESSION_CARDS = "expanded_session_cards"

    private val DEFAULT_DELETE_SESSION_IN_PROGRESS = false
    private val DEFAULT_SESSIONS_TO_REMOVE = false
    private val DEFAULT_CALIBRATION_VALUE = 100
    private val DEFAULT_USE_24_HOUR_FORMAT = true
    private val DEFAULT_USE_CELSIUS_SCALE = false
    private val DEFAULT_CROWD_MAP_ENABLED = true
    private val DEFAULT_MAPS_DISABLED = false
    protected open val DEFAULT_BACKEND_URL = "http://aircasting.org"
    private val DEFAULT_BACKEND_PORT = "80"
    protected open val DEFAULT_ONBOARDING_DISPLAYED = false
    private val DEFAULT_APP_RESTARTED = false
    private val DEFAULT_THEME_VALUE = false
    private val DEFAULT_KEEP_SCREEN_ON = false
    private val DEFAULT_FOLLOWED_SESSIONS_NUMBER = 0
    private val DEFAULT_MOBILE_ACTIVE_SESSIONS = 0

    private val sharedPreferences: SharedPreferences =
        mApplication.getSharedPreferences(PREFERENCES_NAME, PRIVATE_MODE)

    fun getAuthToken(): String? {
        return getStringFromSettings(AUTH_TOKEN_KEY)
    }

    fun getEmail(): String? {
        return getStringFromSettings(EMAIL_KEY)
    }

    fun getProfileName(): String? {
        return getStringFromSettings(PROFILE_NAME_KEY)
    }

    fun getCalibrationValue(): Int {
        return getIntFromSettings(CALIBRATION_KEY, DEFAULT_CALIBRATION_VALUE)
    }

    fun isUsing24HourFormat(): Boolean {
        return getBooleanFromSettings(USE_24_HOUR_FORMAT_KEY, DEFAULT_USE_24_HOUR_FORMAT)
    }

    fun isDarkThemeEnabled(): Boolean {
        return getBooleanFromSettings(THEME_SET_TO_DARK_KEY, DEFAULT_THEME_VALUE)
    }

    fun isCelsiusScaleEnabled(): Boolean {
        return getBooleanFromSettings(USE_CELSIUS_SCALE_KEY, DEFAULT_USE_CELSIUS_SCALE)
    }

    fun isCrowdMapEnabled(): Boolean {
        return getBooleanFromSettings(CROWD_MAP_ENABLED_KEY, DEFAULT_CROWD_MAP_ENABLED)
    }

    fun areMapsDisabled(): Boolean {
        return getBooleanFromSettings(MAPS_DISABLED_KEY, DEFAULT_MAPS_DISABLED)
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

    fun getIsDeleteSessionInProgress(): Boolean {
        return getBooleanFromSettings(
            DELETE_SESSION_IN_PROGERSS_KEY,
            DEFAULT_DELETE_SESSION_IN_PROGRESS
        )
    }

    fun isKeepScreenOnEnabled(): Boolean {
        return getBooleanFromSettings(KEEP_SCREEN_ON_KEY, DEFAULT_KEEP_SCREEN_ON)
    }

    fun thereAreSessionsToRemove(): Boolean {
        return getBooleanFromSettings(SESSIONS_TO_REMOVE_KEY, DEFAULT_SESSIONS_TO_REMOVE)
    }

    fun getFollowedSessionsNumber(): Int {
        return getIntFromSettings(FOLLOWED_SESSIONS_NUMBER_KEY, DEFAULT_FOLLOWED_SESSIONS_NUMBER)
    }

    fun getMobileActiveSessions(): Int {
        return getIntFromSettings(MOBILE_ACTIVE_NUMBERS_KEY, DEFAULT_MOBILE_ACTIVE_SESSIONS)
    }

    fun isUsingSatelliteView(): Boolean {
        return getBooleanFromSettings(USE_SATELLITE_VIEW, false)
    }

    fun toggleUsingSatelliteView() {
        val enabled = !isUsingSatelliteView()
        saveToSettings(USE_SATELLITE_VIEW, enabled)
    }

    fun toggleUse24HourFormatEnabled() {
        val enabled = !isUsing24HourFormat()
        saveToSettings(USE_24_HOUR_FORMAT_KEY, enabled)
    }

    fun toggleUseCelsiusScaleEnabled() {
        val enabled = !isCelsiusScaleEnabled()
        saveToSettings(USE_CELSIUS_SCALE_KEY, enabled)
    }

    fun toggleMapSettingsEnabled() {
        val enabled = !areMapsDisabled()
        saveToSettings(MAPS_DISABLED_KEY, enabled)
    }

    fun toggleCrowdMapEnabled() {
        val enabled = !isCrowdMapEnabled()
        saveToSettings(CROWD_MAP_ENABLED_KEY, enabled)
    }

    fun toggleThemeChangeEnabled() {
        val enabled = !isDarkThemeEnabled()
        saveToSettings(THEME_SET_TO_DARK_KEY, enabled)
    }

    fun toggleKeepScreenOn() {
        val enabled = !isKeepScreenOnEnabled()
        saveToSettings(KEEP_SCREEN_ON_KEY, enabled)
    }

    open fun onboardingAccepted() {
        saveToSettings(ONBOARDING_DISPLAYED_KEY, true)
    }

    open fun onboardingNotDisplayed() {
        saveToSettings(ONBOARDING_DISPLAYED_KEY, false)
    }

    fun microphoneSettingsChanged(calibration: Int) {
        saveToSettings(CALIBRATION_KEY, calibration)
    }

    fun backendSettingsChanged(url: String, port: String) {
        val logoutService = LogoutService(this)

        saveToSettings(BACKEND_URL_KEY, url)
        saveToSettings(BACKEND_PORT_KEY, port)

        logoutService.perform {
            ProcessPhoenix.triggerRebirth(mApplication)
        }
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

    fun login(profileName: String, email: String, authToken: String) {
        saveToSettings(PROFILE_NAME_KEY, profileName)
        saveToSettings(EMAIL_KEY, email)
        saveToSettings(AUTH_TOKEN_KEY, authToken)
    }

    fun increaseFollowedSessionsNumber() {
        saveToSettings(FOLLOWED_SESSIONS_NUMBER_KEY, getFollowedSessionsNumber() + 1)
    }

    fun decreaseFollowedSessionsNumber() {
        saveToSettings(FOLLOWED_SESSIONS_NUMBER_KEY, getFollowedSessionsNumber() - 1)
    }

    fun increaseActiveMobileSessionsNumber() {
        saveToSettings(MOBILE_ACTIVE_NUMBERS_KEY, getFollowedSessionsNumber() + 1)
    }

    fun decreaseActiveMobileSessionsNumber() {
        saveToSettings(MOBILE_ACTIVE_NUMBERS_KEY, getFollowedSessionsNumber() - 1)
    }

    fun saveExpandedSessionsUUIDs(uuids: Set<String>) {
        saveToSettings(EXPANDED_SESSION_CARDS, uuids)
    }

    fun getExpandedSessionsUUIDs() =
        getStringSetFromSettings(EXPANDED_SESSION_CARDS)?.toMutableSet() ?: mutableSetOf()

    open fun logout() {
        deleteFromSettings()
    }

    open fun getStringSetFromSettings(key: String, default: Set<String>? = null): Set<String>? {
        return sharedPreferences.getStringSet(key, default)
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
        editor.apply()
    }

    protected open fun saveToSettings(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    protected open fun saveToSettings(key: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    protected open fun saveToSettings(key: String, value: Set<String>) {
        val editor = sharedPreferences.edit()
        editor.putStringSet(key, value)
        editor.apply()
    }

    private fun deleteFromSettings() {
        val keys = sharedPreferences.all.keys
        val editor = sharedPreferences.edit()
        for (key in keys) {
            if (key != BACKEND_URL_KEY && key != BACKEND_PORT_KEY) {
                editor.remove(key)
                editor.apply()
            }
        }
    }
}
