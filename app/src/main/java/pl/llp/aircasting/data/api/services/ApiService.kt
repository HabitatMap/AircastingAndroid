package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.Constants
import pl.llp.aircasting.data.api.params.*
import pl.llp.aircasting.data.api.response.*
import pl.llp.aircasting.data.api.response.search.SessionsInRegionsRes
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET(Constants.urlDownloadSession)
    fun downloadSession(@Query("uuid") uuid: String): Call<SessionResponse>

    @GET(Constants.urlDownloadSession)
    fun downloadSessionWithMeasurements(
        @Query("uuid") uuid: String,
        @Query("stream_measurements") streamMeasurements: Boolean = true
    ): Call<SessionWithMeasurementsResponse>

    @GET(Constants.urlDownloadFixedMeasurements)
    fun downloadFixedMeasurements(
        @Query("uuid") uuid: String,
        @Query("last_measurement_sync") lastMeasurementSync: String
    ): Call<SessionWithMeasurementsResponse>

    @GET(Constants.urlLogin)
    fun login(): Call<UserResponse>

    @GET(Constants.urlExportSession)
    fun exportSession(
        @Query("email") email: String,
        @Query("uuid") uuid: String
    ): Call<ExportSessionResponse>

    @GET(Constants.urlSessionInGivenLocation)
    suspend fun getSessionsInRegion(@Query("q") query: String): SessionsInRegionsRes

    @GET(Constants.urlStreamOfGivenSession)
    suspend fun getStreamOfGivenSession(
        @Path("sessionID") sessionID: Long,
        @Query("sensor_name") sensorName: String,
        @Query("measurements_limit") measurementsLimit: Int
        ): StreamOfGivenSessionResponse

    /* POST Requests */
    @POST(Constants.urlCreateMobileSession)
    fun createMobileSession(@Body body: CreateSessionBody): Call<UploadSessionResponse>

    @POST(Constants.urlCreateFixedSession)
    fun createFixedSession(@Body body: CreateSessionBody): Call<UploadSessionResponse>

    @POST(Constants.urlSync)
    fun sync(@Body body: SyncSessionBody): Call<SyncResponse>

    @POST(Constants.urlCreateAccount)
    fun createAccount(@Body body: CreateAccountBody): Call<UserResponse>

    @POST(Constants.urlUpdateSession)
    fun updateSession(@Body body: UpdateSessionBody): Call<UpdateSessionResponse>

    @POST(Constants.urlResetPassword)
    fun resetPassword(@Body body: ForgotPasswordBody): Call<ForgotPasswordResponse>

    @POST(Constants.urlUploadFixedMeasurements)
    fun uploadFixedMeasurements(@Body body: UploadFixedMeasurementsBody): Call<Unit>
}