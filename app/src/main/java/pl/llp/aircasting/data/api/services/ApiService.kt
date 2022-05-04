package pl.llp.aircasting.data.api.services

import android.util.Base64
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import pl.llp.aircasting.BuildConfig
import pl.llp.aircasting.data.api.Constants
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

    @GET(Constants.urlDownloadSession)
    fun downloadSession(@Query("uuid") uuid: String): Call<SessionResponse>

    @GET(Constants.urlDownloadSession)
    fun downloadSessionWithMeasurements(
        @Query("uuid") uuid: String,
        @Query("stream_measurements") stream_measurements: Boolean = true
    ): Call<SessionWithMeasurementsResponse>

    @GET(Constants.urlDownloadFixedMeasurements)
    fun downloadFixedMeasurements(
        @Query("uuid") uuid: String,
        @Query("last_measurement_sync") last_measurement_sync: String
    ): Call<SessionWithMeasurementsResponse>

    @GET(Constants.urlLogin)
    fun login(): Call<UserResponse>

    @GET(Constants.urlExportSession)
    fun exportSession(
        @Query("email") email: String,
        @Query("uuid") uuid: String
    ): Call<ExportSessionResponse>

    @GET(Constants.urlSessionInGivenLocation)
    fun getSessionsInRegion(
        @Query("north") north: Double,
        @Query("south") south: Double,
        @Query("east") east: Double,
        @Query("west") west: Double,
        @Query("time_from") time_from: Int,
        @Query("time_to") time_to: Int,
        @Query("measurement_type") measurement_type: String,
        @Query("sensor_name") sensor_name: String,
        @Query("unit_symbol") unit_symbol: String,
        @Query("tags") tags: String,
        @Query("usernames") usernames: String
    ): Call<SessionsInRegionResponse>

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
        val baseUrl = mSettings.getBackendUrl() + ":" + mSettings.getBackendPort()

        return if (mSettings.getBackendUrl()?.last().toString() == URL_SUFFIX) {
            baseUrl.toHttpUrl()
        } else {
            (baseUrl + URL_SUFFIX).toHttpUrl()
        }
    }

    private fun encodedCredentials(username: String, password: String): String {
        val credentials = "${username}:${password}"
        val encodedCredentials = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        return "Basic $encodedCredentials"
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
