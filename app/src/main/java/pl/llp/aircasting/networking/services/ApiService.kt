package pl.llp.aircasting.networking.services

import android.util.Base64
import pl.llp.aircasting.BuildConfig
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.networking.params.*
import pl.llp.aircasting.networking.responses.*
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
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

    @GET("/api/user/sessions/empty.json")
    fun downloadSessionWithMeasurements(@Query("uuid") uuid: String, @Query("stream_measurements") stream_measurements: Boolean = true): Call<SessionWithMeasurementsResponse>

    @POST("/api/user/sessions/sync_with_versioning.json")
    fun sync(@Body body: SyncSessionBody): Call<SyncResponse>

    @GET("/api/realtime/sync_measurements.json")
    fun downloadFixedMeasurements(@Query("uuid") uuid: String, @Query("last_measurement_sync") last_measurement_sync: String): Call<SessionWithMeasurementsResponse>

    @GET("/api/user.json")
    fun login(): Call<UserResponse>

    @POST("/api/user.json")
    fun createAccount(@Body body: CreateAccountBody): Call<UserResponse>

    @POST("/api/user/sessions/update_session.json")
    fun updateSession(@Body body: UpdateSessionBody): Call<UpdateSessionResponse>

    @GET("/api/sessions/export_by_uuid.json")
    fun exportSession(@Query("email") email: String, @Query("uuid") uuid: String): Call<ExportSessionResponse>

    @POST("/users/password.json")
    fun resetPassword(@Body body: ForgotPasswordBody): Call<ForgotPasswordResponse>

    @POST("/api/realtime/measurements")
    fun uploadFixedMeasurements(@Body body: UploadFixedMeasurementsBody): Call<Unit>
}

open class ApiServiceFactory(private val mSettings: Settings) {
    private val READ_TIMEOUT_SECONDS: Long = 60
    private val CONNECT_TIMEOUT_SECONDS: Long = 60
    private lateinit var baseUrl : HttpUrl

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

        baseUrl()
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .baseUrl(baseUrl)
            .build()

        return retrofit.create<ApiService>(ApiService::class.java)
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

    protected open fun baseUrl() {
        if (mSettings.getBackendUrl()?.last()?.equals("/") == true) {
            baseUrl = HttpUrl.get(mSettings.getBackendUrl() + ":" + mSettings.getBackendPort())
        } else {
            baseUrl = HttpUrl.get(mSettings.getBackendUrl() + ":" + mSettings.getBackendPort() + "/")
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
