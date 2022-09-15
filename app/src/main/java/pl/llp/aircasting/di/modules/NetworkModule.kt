package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.llp.aircasting.BuildConfig
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.util.ApiConstants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {
    private val READ_TIMEOUT_SECONDS: Long = 60
    private val CONNECT_TIMEOUT_SECONDS: Long = 60

    @Provides
    fun provideBaseUrl() = ApiConstants.baseUrl + "/"

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): ApiService =
        Retrofit.Builder()
            .baseUrl(provideBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)

    @Provides
    @Singleton
    fun providesOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return if (BuildConfig.DEBUG)
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        else
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.NONE
            }
    }

}