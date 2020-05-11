package io.lunarlogic.aircasting.networking

import io.lunarlogic.aircasting.BuildConfig
import io.lunarlogic.aircasting.sensor.Session
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Base64
import retrofit2.http.*


interface ApiService {
    @POST("/api/sessions")
    fun createSession(@Body body: CreateSessionBody): Call<Session>

    @GET("/api/user.json")
    fun login(): Call<UserResponse>
}

class ApiServiceFactory {
    companion object {
        private val BASE_URL = "http://aircasting.org/"

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
            val httpClient = httpClientBuilder.build()


            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .baseUrl(BASE_URL)
                .build()

            return retrofit.create<ApiService>(ApiService::class.java)
        }

        fun get(username: String, password: String): ApiService {
            val credentialsEncoded = encodedCredentials(username, password)
            val authInterceptor = AuthenticationInterceptor(credentialsEncoded)

            return get(listOf(authInterceptor))
        }

        fun get(authToken: String): ApiService {
            val credentialsEncoded = encodedCredentials(authToken, "X")
            val authInterceptor = AuthenticationInterceptor(credentialsEncoded)

            return get(listOf(authInterceptor))
        }

        private fun encodedCredentials(username: String, password: String): String {
            val credentials = "${username}:${password}"
            val encodedCredentials = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
            return "Basic ${encodedCredentials}"
        }
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