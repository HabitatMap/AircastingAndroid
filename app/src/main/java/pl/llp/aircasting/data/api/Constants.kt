package pl.llp.aircasting.data.api

object Constants {
    const val baseUrl = "http://aircasting.org/"

    /* GET Requests */
    const val urlSessionInGivenLocation = "/api/fixed/active/sessions.json"
    const val urlStreamOfGivenSession = "/api/fixed/sessions/{sessionID}.json"
    const val urlSessionWithStreamsAndMeasurements = "/api/fixed/sessions/{sessionID}/streams.json"
    const val urlDownloadSession = "/api/user/sessions/empty.json"
    const val urlDownloadFixedMeasurements = "/api/realtime/sync_measurements.json"
    const val urlLogin = "/api/user.json"
    const val urlExportSession = "/api/sessions/export_by_uuid.json"

    /* POST Requests */
    const val urlCreateMobileSession = "/api/sessions"
    const val urlCreateFixedSession = "/api/realtime/sessions.json"
    const val urlSync = "/api/user/sessions/sync_with_versioning.json"
    const val urlCreateAccount = "/api/user.json"
    const val urlUpdateSession = "/api/user/sessions/update_session.json"
    const val urlResetPassword = "/users/password.json"
    const val urlUploadFixedMeasurements = "/api/realtime/measurements"

    /* Milliseconds */
    const val MILLIS_IN_HOUR = 60 * 60 * 1000
    const val MILLIS_IN_MINUTE = 60 * 1000
    const val MILLIS_IN_SECOND = 1000

    /* Miscellaneous */
    const val MEASUREMENTS_IN_HOUR = 60

    /* Strings for constructing requests */
    const val airbeam2sensorName = "airbeam2-pm2.5"
    const val airbeam3sensorName = "airbeam3-pm2.5"
    const val openAQsensorNamePM = "openaq-pm2.5"
    const val purpleAirSensorName = "purpleair-pm2.5"
    const val openAQsensorNameOzone = "openaq-o3"

    const val measurementTypePM = "Particulate Matter"
    const val measurementTypeOzone = "Ozone"

    const val nanoGrammsPerCubicMeter = "µg/m³"
    const val partsPerBillion = "ppb"


    /* Strings from responses */
    const val responseAirbeam2SensorName = "AirBeam2-PM2.5"
    const val responseAirbeam3SensorName = "AirBeam3-PM2.5"
    const val responsePurpleAirSensorName = "PurpleAir-PM2.5"
    const val responseOpenAQSensorNamePM = "OpenAQ-PM2.5"
    const val responseOpenAQSensorNameOzone = "OpenAQ-O3"
}
