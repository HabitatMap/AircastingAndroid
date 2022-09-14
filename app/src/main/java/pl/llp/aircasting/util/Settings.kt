package pl.llp.aircasting.util

import android.content.SharedPreferences

open class Settings(private val sharedPreferences: SharedPreferences) {
    companion object {
        const val PREFERENCES_NAME = "preferences"
        private const val PROFILE_NAME_KEY = "profile_name"
        private const val EMAIL_KEY = "email"
        private const val AUTH_TOKEN_KEY = "auth_token"
        private const val USE_24_HOUR_FORMAT_KEY = "use_24_hour_format"
        private const val USE_CELSIUS_SCALE_KEY = "use_celsius_scale"
        private const val CROWD_MAP_ENABLED_KEY = "crowd_map"
        private const val CALIBRATION_KEY = "calibration"
        private const val MAPS_DISABLED_KEY = "maps_disabled"
        private const val BACKEND_URL_KEY = "backend_url"
        private const val BACKEND_PORT_KEY = "backend_port"
        private const val ONBOARDING_DISPLAYED_KEY = "onboarding_displayed"
        private const val APP_RESTARTED = "app_restarted"

        private const val FOLLOWED_SESSIONS_NUMBER_KEY = "followed_sesions_number"
        private const val MOBILE_ACTIVE_NUMBERS_KEY = "mobile_active_sessions"
        private const val DEFAULT_MOBILE_ACTIVE_SESSIONS = 0
        private const val THEME_SET_TO_DARK_KEY = "theme_dark"
        private const val KEEP_SCREEN_ON_KEY = "keep_screen_on"
        private const val USE_SATELLITE_VIEW = "use_satellite_view"

        private const val DELETE_SESSION_IN_PROGRESS_KEY = "delete_session_in_progress"
        private const val SESSIONS_TO_REMOVE_KEY = "sessions_to_remove"
        private const val EXPANDED_SESSION_CARDS = "expanded_session_cards"

        private const val DEFAULT_DELETE_SESSION_IN_PROGRESS = false
        private const val DEFAULT_SESSIONS_TO_REMOVE = false
        private const val DEFAULT_CALIBRATION_VALUE = 100
        private const val DEFAULT_USE_24_HOUR_FORMAT = true
        private const val DEFAULT_USE_CELSIUS_SCALE = false
        private const val DEFAULT_CROWD_MAP_ENABLED = true
        private const val DEFAULT_MAPS_DISABLED = false

        private const val DEFAULT_BACKEND_PORT = "80"

        private const val DEFAULT_APP_RESTARTED = false
        private const val DEFAULT_THEME_VALUE = false
        private const val DEFAULT_KEEP_SCREEN_ON = false
        private const val DEFAULT_FOLLOWED_SESSIONS_NUMBER = 0
        private const val DEFAULT_ONBOARDING_DISPLAYED = false
    }

    protected open val DEFAULT_BACKEND_URL = "http://aircasting.org"

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
            DELETE_SESSION_IN_PROGRESS_KEY,
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

    fun mobileActiveSessionsCount(): Int {
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

    fun saveUrlAndPort(url: String, port: String) {
        saveToSettings(BACKEND_URL_KEY, url)
        saveToSettings(BACKEND_PORT_KEY, port)
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
        saveToSettings(DELETE_SESSION_IN_PROGRESS_KEY, deleteInProgress)
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
        saveToSettings(MOBILE_ACTIVE_NUMBERS_KEY, mobileActiveSessionsCount() + 1)
    }

    fun decreaseActiveMobileSessionsNumber() {
        saveToSettings(MOBILE_ACTIVE_NUMBERS_KEY, mobileActiveSessionsCount() - 1)
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
