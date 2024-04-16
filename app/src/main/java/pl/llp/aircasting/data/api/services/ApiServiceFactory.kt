package pl.llp.aircasting.data.api.services

import android.util.Base64
import android.util.Log
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.llp.aircasting.BuildConfig
import pl.llp.aircasting.data.api.interceptor.AuthenticationInterceptor
import pl.llp.aircasting.data.api.interceptor.NetworkConnectionInterceptor
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.util.Settings
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier

open class ApiServiceFactory(
    private val settings: Settings,
    private val networkConnectionInterceptor: NetworkConnectionInterceptor,
) {

    @NonAuthenticated
    fun getNonAuthenticated(): ApiService {
        return getApiService(emptyList())
    }

    private val READ_TIMEOUT_SECONDS: Long = 60
    private val CONNECT_TIMEOUT_SECONDS: Long = 60

    fun getAuthenticatedWithCredentials(username: String, password: String): ApiService {
        val credentialsEncoded = encodedCredentials(username, password)
        val authInterceptor = AuthenticationInterceptor(credentialsEncoded)

        return getApiService(listOf(authInterceptor))
    }

    @Authenticated
    fun getAuthenticated(authToken: String?): ApiService {
        if (authToken == null) {
            Log.e(TAG, "Auth token was null")
            return getNonAuthenticated()
        }
        Log.e(TAG, "Auth token was NOT null")

        val credentialsEncoded = encodedCredentials(authToken, "X")
        val authInterceptor = AuthenticationInterceptor(credentialsEncoded)

        return getApiService(listOf(authInterceptor))
    }

    protected open fun baseUrl(): HttpUrl {
        val suffix = "/"
        val backendUrl = settings.getBackendUrl()

        var baseUrl = backendUrl + ":" + settings.getBackendPort()

        if (baseUrl.last().toString() != suffix)
            baseUrl += suffix

        return baseUrl.toHttpUrl()
    }

    private fun getApiService(additionalInterceptors: List<Interceptor>): ApiService {
        val logging = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            logging.level = HttpLoggingInterceptor.Level.BODY
        } else {
            logging.level = HttpLoggingInterceptor.Level.BASIC
        }

        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.addInterceptor(logging)
        httpClientBuilder.addInterceptor(networkConnectionInterceptor)
        additionalInterceptors.forEach { interceptor -> httpClientBuilder.addInterceptor(interceptor) }

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

    private fun encodedCredentials(username: String, password: String): String {
        val credentials = "${username}:${password}"
        val encodedCredentials = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        return "Basic $encodedCredentials"
    }
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Authenticated

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class NonAuthenticated