package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Base64
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.params.CreateAccountBody
import io.lunarlogic.aircasting.networking.params.CreateSessionBody
import io.lunarlogic.aircasting.networking.params.ExportSessionBody
import io.lunarlogic.aircasting.networking.params.SyncSessionBody
import io.lunarlogic.aircasting.networking.params.UpdateSessionBody
import io.lunarlogic.aircasting.networking.responses.*
import okhttp3.HttpUrl
import retrofit2.http.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


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
    fun downloadMeasurements(@Query("uuid") uuid: String, @Query("last_measurement_sync") last_measurement_sync: String): Call<SessionWithMeasurementsResponse>

    @GET("/api/user.json")
    fun login(): Call<UserResponse>

    @POST("/api/user.json")
    fun createAccount(@Body body: CreateAccountBody): Call<UserResponse>

    @POST("/api/user/sessions/update_session.json")
    fun updateSession(@Body body: UpdateSessionBody): Call<UpdateSessionResponse>

    // TODO: this call needs a huge verification (for sure call argument is now random)
    @GET("/api/sessions/export_by_uuid.json")
    fun sendSessionByEmail(@Body body: ExportSessionBody): Call<ExportSessionResponse>
}

open class ApiServiceFactory(private val mSettings: Settings) {
    private val READ_TIMEOUT_SECONDS: Long = 60

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
            .build()


        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .baseUrl(baseUrl())
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

    protected open fun baseUrl() : HttpUrl {
        return HttpUrl.get(mSettings.getBackendUrl() + ":" + mSettings.getBackendPort())
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
