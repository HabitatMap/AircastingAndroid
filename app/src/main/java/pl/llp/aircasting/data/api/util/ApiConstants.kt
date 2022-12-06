package pl.llp.aircasting.data.api.util

object ApiConstants {
    const val baseUrl = "http://aircasting.org"

    /* GET Requests */
    const val urlSessionInGivenLocation = "/api/fixed/active/sessions.json"
    const val urlStreamOfGivenSession = "/api/fixed/sessions/{sessionID}.json"
    const val urlSessionWithStreamsAndMeasurements = "/api/fixed/sessions/{sessionID}/streams.json"
    const val urlDownloadSession = "/api/user/sessions/empty.json"
    const val urlDownloadFixedMeasurements = "/api/realtime/sync_measurements.json"
    const val urlLogin = "/api/user.json"
    const val urlExportSession = "/api/sessions/export_by_uuid.json"
    const val urlGetThresholdAlerts = "api/fixed/threshold_alerts"

    /* POST Requests */
    const val urlCreateFixedSession = "/api/realtime/sessions.json"
    const val urlCreateMobileSession = "/api/sessions"
    const val urlSync = "/api/user/sessions/sync_with_versioning.json"
    const val urlCreateAccount = "/api/user.json"
    const val urlUpdateSession = "/api/user/sessions/update_session.json"
    const val urlResetPassword = "/users/password.json"
    const val urlUploadFixedMeasurements = "/api/realtime/measurements"
    const val urlUpdateUserSettings = "/api/user/settings"
    const val urlCreateThresholdAlert = "api/fixed/threshold_alerts"

    /* DELETE Requests */
    const val urlDeleteThresholdAlert = "api/fixed/threshold_alerts/{id}"
}
