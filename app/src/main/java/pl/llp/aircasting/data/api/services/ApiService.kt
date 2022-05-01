package pl.llp.aircasting.data.api.services

import android.util.Base64
import io.reactivex.Single
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import pl.llp.aircasting.BuildConfig
import pl.llp.aircasting.data.api.params.*
import pl.llp.aircasting.data.api.responses.*
import pl.llp.aircasting.util.Settings
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit


interface ApiService {
    @POST("/api/sessions")
    fun createMobileSession(@Body body: CreateSessionBody): Call<UploadSessionResponse>

    @POST("/api/realtime/sessions.json")
    fun createFixedSession(@Body body: CreateSessionBody): Call<UploadSessionResponse>

    @GET("/api/user/sessions/empty.json")
    fun downloadSession(@Query("uuid") uuid: String): Call<SessionResponse>

    @GET("/api/fixed/active/sessions.json")
    fun getSessionsInRegion(
        @Query("north") north: Double = 0.0,
        @Query("south") south: Double = 0.0,
        @Query("east") east: Double = 0.0,
        @Query("west") west: Double = 0.0,
//        @Query("time_from") time_from: Int = 0,
//        @Query("time_to") time_to: Int = 0,
//        @Query("measurement_type") measurement_type: String = "",
//        @Query("sensor_name") sensor_name: String = "",
//        @Query("unit_symbol") unit_symbol: String = "",
//        @Query("tags") tags: String = "",
//        @Query("usernames") usernames: String = ""
    ): Call<SessionsInRegionResponse>

    @GET("/api/fixed/active/sessions.json")
    fun getSessionInRegion(): Single<SessionInRegionResponse>

    @GET("/api/user/sessions/empty.json")
    fun downloadSessionWithMeasurements(
        @Query("uuid") uuid: String,
        @Query("stream_measurements") stream_measurements: Boolean = true
    ): Call<SessionWithMeasurementsResponse>

    @POST("/api/user/sessions/sync_with_versioning.json")
    fun sync(@Body body: SyncSessionBody): Call<SyncResponse>

    @GET("/api/realtime/sync_measurements.json")
    fun downloadFixedMeasurements(
        @Query("uuid") uuid: String,
        @Query("last_measurement_sync") last_measurement_sync: String
    ): Call<SessionWithMeasurementsResponse>

    @GET("/api/user.json")
    fun login(): Call<UserResponse>

    @POST("/api/user.json")
    fun createAccount(@Body body: CreateAccountBody): Call<UserResponse>

    @POST("/api/user/sessions/update_session.json")
    fun updateSession(@Body body: UpdateSessionBody): Call<UpdateSessionResponse>

    @GET("/api/sessions/export_by_uuid.json")
    fun exportSession(
        @Query("email") email: String,
        @Query("uuid") uuid: String
    ): Call<ExportSessionResponse>

    @POST("/users/password.json")
    fun resetPassword(@Body body: ForgotPasswordBody): Call<ForgotPasswordResponse>

    @POST("/api/realtime/measurements")
    fun uploadFixedMeasurements(@Body body: UploadFixedMeasurementsBody): Call<Unit>
}

open class ApiServiceFactory(private val mSettings: Settings) {
    private val READ_TIMEOUT_SECONDS: Long = 60
    private val CONNECT_TIMEOUT_SECONDS: Long = 60

    fun get(interceptors: List<Interceptor>): ApiService {
        val logging = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            logging.level = HttpLoggingInterceptor.Level.BODY
        } else {
            logging.level = HttpLoggingInterceptor.Level.BASIC
        }

        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.addInterceptor(logging)
        interceptors.forEach { interceptor -> httpClientBuilder.addInterceptor(interceptor) }
        val httpClient = httpClientBuilder
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()


        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .baseUrl(baseUrl())
            .build()

        return retrofit.create(ApiService::class.java)
    }

    fun get(username: String, password: String): ApiService {
        val credentialsEncoded =
            encodedCredentials(
                username,
                password
            )
        val authInterceptor =
            AuthenticationInterceptor(
                credentialsEncoded
            )

        return get(
            listOf(authInterceptor)
        )
    }

    fun get(authToken: String): ApiService {
        val credentialsEncoded =
            encodedCredentials(
                authToken,
                "X"
            )
        val authInterceptor =
            AuthenticationInterceptor(
                credentialsEncoded
            )

        return get(
            listOf(authInterceptor)
        )
    }

    protected open fun baseUrl(): HttpUrl {
        val URL_SUFFIX = "/"
        var baseUrl = mSettings.getBackendUrl() + ":" + mSettings.getBackendPort()

        if (mSettings.getBackendUrl()?.last()?.equals(URL_SUFFIX) == true) {
            return baseUrl.toHttpUrl()
        } else {
            return (baseUrl + URL_SUFFIX).toHttpUrl()
        }
    }

    private fun encodedCredentials(username: String, password: String): String {
        val credentials = "${username}:${password}"
        val encodedCredentials = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        return "Basic ${encodedCredentials}"
    }

}

class AuthenticationInterceptor(private val authToken: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val builder = original.newBuilder()
            .header("Authorization", authToken)

        val request = builder.build()
        return chain.proceed(request)
    }
}
