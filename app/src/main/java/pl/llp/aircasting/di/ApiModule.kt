package pl.llp.aircasting.di

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.api.services.UploadFixedMeasurementsService
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import javax.inject.Singleton

open class WebServerFactory

@Module
open class ApiModule {
    @Provides
    @Singleton
    open fun providesMockWebServerFactory(): WebServerFactory = WebServerFactory()

    @Provides
    @Singleton
    open fun providesApiServiceFactory(
        settings: Settings,
        webServerFactory: WebServerFactory
    ): ApiServiceFactory = ApiServiceFactory(settings)

    @Provides
    @Singleton
    fun providesUploadFixedMeasurementsService(
        apiServiceFactory: ApiServiceFactory,
        settings: Settings,
        errorHandler: ErrorHandler
    ): UploadFixedMeasurementsService? {
        val authToken = settings.getAuthToken() ?: return null

        val apiService = apiServiceFactory.get(authToken)
        return UploadFixedMeasurementsService(apiService, errorHandler)
    }

    @Provides
    fun providesSessionsSyncService(
        apiServiceFactory: ApiServiceFactory,
        settings: Settings,
        errorHandler: ErrorHandler
    ): SessionsSyncService? {
        val authToken = settings.getAuthToken() ?: return null

        val apiService = apiServiceFactory.get(authToken)
        return SessionsSyncService.get(apiService, errorHandler, settings)
    }
}
