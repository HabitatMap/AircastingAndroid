package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.params.*
import pl.llp.aircasting.data.api.response.*
import pl.llp.aircasting.data.api.response.search.SessionsInRegionsResponse
import pl.llp.aircasting.data.api.response.search.session.details.SessionWithStreamsAndMeasurementsResponse
import pl.llp.aircasting.data.api.util.ApiConstants
import pl.llp.aircasting.data.api.util.ApiConstants.urlCreateAccount
import pl.llp.aircasting.data.api.util.ApiConstants.urlCreateFixedSession
import pl.llp.aircasting.data.api.util.ApiConstants.urlCreateMobileSession
import pl.llp.aircasting.data.api.util.ApiConstants.urlCreateThresholdAlert
import pl.llp.aircasting.data.api.util.ApiConstants.urlDeleteThresholdAlert
import pl.llp.aircasting.data.api.util.ApiConstants.urlExportSession
import pl.llp.aircasting.data.api.util.ApiConstants.urlGetThresholdAlerts
import pl.llp.aircasting.data.api.util.ApiConstants.urlResetPassword
import pl.llp.aircasting.data.api.util.ApiConstants.urlSessionInGivenLocation
import pl.llp.aircasting.data.api.util.ApiConstants.urlSessionWithStreamsAndMeasurements
import pl.llp.aircasting.data.api.util.ApiConstants.urlStreamOfGivenSession
import pl.llp.aircasting.data.api.util.ApiConstants.urlSync
import pl.llp.aircasting.data.api.util.ApiConstants.urlUpdateSession
import pl.llp.aircasting.data.api.util.ApiConstants.urlUpdateUserSettings
import pl.llp.aircasting.data.api.util.ApiConstants.urlUploadFixedMeasurements
import pl.llp.aircasting.data.api.util.ApiConstants.urlUser
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET(ApiConstants.urlDownloadSession)
    suspend fun downloadSession(@Query("uuid") uuid: String): SessionResponse

    @GET(ApiConstants.urlDownloadSession)
    suspend fun downloadSessionWithMeasurements(
        @Query("uuid") uuid: String,
        @Query("stream_measurements") streamMeasurements: Boolean = true
    ): SessionWithMeasurementsResponse

    @GET(ApiConstants.urlDownloadFixedMeasurements)
    suspend fun downloadFixedMeasurements(
        @Query("uuid") uuid: String,
        @Query("last_measurement_sync") lastMeasurementSync: String
    ): SessionWithMeasurementsResponse

    @GET(urlUser)
    fun login(): Call<UserResponse>

    @GET(urlUser)
    suspend fun loginSuspend(): Response<UserResponse>

    @GET(urlExportSession)
    fun exportSession(
        @Query("email") email: String,
        @Query("uuid") uuid: String
    ): Call<ExportSessionResponse>

    @GET(urlSessionInGivenLocation)
    suspend fun getSessionsInRegion(@Query("q") query: String): SessionsInRegionsResponse

    @GET(urlStreamOfGivenSession)
    suspend fun getStreamOfGivenSession(
        @Path("sessionID") sessionID: Long,
        @Query("sensor_name") sensorName: String,
        @Query("measurements_limit") measurementsLimit: Int
    ): StreamOfGivenSessionResponse

    @GET(urlSessionWithStreamsAndMeasurements)
    suspend fun getSessionWithStreamsAndMeasurements(
        @Path("sessionID") sessionID: Long,
        @Query("measurements_limit") measurementsLimit: Int
    ): SessionWithStreamsAndMeasurementsResponse

    @GET(urlGetThresholdAlerts)
    suspend fun getThresholdAlerts(): List<ThresholdAlertResponse>

    /* POST Requests */
    @POST(urlCreateMobileSession)
    suspend fun createMobileSession(@Body body: CreateSessionBody): Response<UploadSessionResponse>

    @POST(urlCreateFixedSession)
    suspend fun createFixedSession(@Body body: CreateSessionBody): Response<UploadSessionResponse>

    @POST(urlSync)
    suspend fun sync(@Body body: SyncSessionBody): Response<SyncResponse>

    @POST(urlCreateAccount)
    suspend fun createAccount(@Body body: CreateAccountBody): Response<UserResponse>

    @POST(urlUpdateSession)
    suspend fun updateSession(@Body body: UpdateSessionBody): Response<UpdateSessionResponse>

    @POST(urlResetPassword)
    fun resetPassword(@Body body: ForgotPasswordBody): Call<ForgotPasswordResponse>

    @POST(urlUploadFixedMeasurements)
    suspend fun uploadFixedMeasurements(@Body body: UploadFixedMeasurementsBody): Response<Unit>

    @POST(urlUpdateUserSettings)
    suspend fun updateUserSettings(@Body body: UserSettingsBody): Response<UserSettingsResponse>

    @POST(urlCreateThresholdAlert)
    suspend fun createThresholdAlert(@Body body: CreateThresholdAlertBody): CreateThresholdAlertResponse

    /* DELETE Requests */
    @DELETE(urlDeleteThresholdAlert)
    suspend fun deleteThresholdAlert(@Path("id") id: Int)

    @DELETE(urlUser)
    suspend fun deleteAccount(): Response<DeleteAccountResponse?>
}