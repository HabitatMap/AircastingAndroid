package io.lunarlogic.aircasting.networking

import io.lunarlogic.aircasting.BuildConfig
import io.lunarlogic.aircasting.sensor.Session
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Base64


interface ApiService {
    @POST("/api/sessions")
    fun createSession(@Body body: CreateSessionBody): Call<Session>
}

class ApiServiceFactory {
    private val BASE_URL = "http://aircasting.org/"
    private var apiService: ApiService?

    init {
        val logging = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        }

        val credentials = "Eqha7roSkYfgKvyLYHHx" + ":" + "X"
        val authToken = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        val auth = AuthenticationInterceptor(authToken)

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(auth)
            .build()


        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .baseUrl(BASE_URL)
            .build()

        apiService = retrofit.create<ApiService>(ApiService::class.java)
    }

    fun get(): ApiService {
        return apiService!!
    }
}

class AuthenticationInterceptor(private val authToken: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val builder = original.newBuilder()
            .header("Authorization", authToken)
            .header("Accept", "application/json"); // TODO: needed?

        val request = builder.build()
        return chain.proceed(request)
    }
}