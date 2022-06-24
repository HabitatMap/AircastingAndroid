package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.util.ApiConstants
import pl.llp.aircasting.data.api.params.*
import pl.llp.aircasting.data.api.response.*
import pl.llp.aircasting.data.api.response.search.SessionsInRegionsRes
import pl.llp.aircasting.data.api.response.search.session.details.SessionWithStreamsAndMeasurementsResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET(ApiConstants.urlDownloadSession)
    fun downloadSession(@Query("uuid") uuid: String): Call<SessionResponse>

    @GET(ApiConstants.urlDownloadSession)
    fun downloadSessionWithMeasurements(
        @Query("uuid") uuid: String,
        @Query("stream_measurements") streamMeasurements: Boolean = true
    ): Call<SessionWithMeasurementsResponse>

    @GET(ApiConstants.urlDownloadFixedMeasurements)
    fun downloadFixedMeasurements(
        @Query("uuid") uuid: String,
        @Query("last_measurement_sync") lastMeasurementSync: String
    ): Call<SessionWithMeasurementsResponse>

    @GET(ApiConstants.urlLogin)
    fun login(): Call<UserResponse>

    @GET(ApiConstants.urlExportSession)
    fun exportSession(
        @Query("email") email: String,
        @Query("uuid") uuid: String
    ): Call<ExportSessionResponse>

    @GET(ApiConstants.urlSessionInGivenLocation)
    suspend fun getSessionsInRegion(@Query("q") query: String): SessionsInRegionsRes

    @GET(ApiConstants.urlStreamOfGivenSession)
    suspend fun getStreamOfGivenSession(
        @Path("sessionID") sessionID: Long,
        @Query("sensor_name") sensorName: String,
        @Query("measurements_limit") measurementsLimit: Int
        ): StreamOfGivenSessionResponse

    @GET(ApiConstants.urlSessionWithStreamsAndMeasurements)
    suspend fun getSessionWithStreamsAndMeasurements(
        @Path("sessionID") sessionID: Long,
        @Query("measurements_limit") measurementsLimit: Int
    ): SessionWithStreamsAndMeasurementsResponse

    /* POST Requests */
    @POST(ApiConstants.urlCreateMobileSession)
    fun createMobileSession(@Body body: CreateSessionBody): Call<UploadSessionResponse>

    @POST(ApiConstants.urlCreateFixedSession)
    fun createFixedSession(@Body body: CreateSessionBody): Call<UploadSessionResponse>

    @POST(ApiConstants.urlSync)
    fun sync(@Body body: SyncSessionBody): Call<SyncResponse>

    @POST(ApiConstants.urlCreateAccount)
    fun createAccount(@Body body: CreateAccountBody): Call<UserResponse>

    @POST(ApiConstants.urlUpdateSession)
    fun updateSession(@Body body: UpdateSessionBody): Call<UpdateSessionResponse>

    @POST(ApiConstants.urlResetPassword)
    fun resetPassword(@Body body: ForgotPasswordBody): Call<ForgotPasswordResponse>

    @POST(ApiConstants.urlUploadFixedMeasurements)
    fun uploadFixedMeasurements(@Body body: UploadFixedMeasurementsBody): Call<Unit>
}