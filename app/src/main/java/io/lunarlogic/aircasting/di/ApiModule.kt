package io.lunarlogic.aircasting.di

import android.accounts.AccountManager
import dagger.Module
import dagger.Provides
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.AuthenticationHelper
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.SessionsSyncService
import io.lunarlogic.aircasting.networking.services.UploadFixedMeasurementsService
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
        errorHandler: ErrorHandler,
        authenticationHelper: AuthenticationHelper
    ): UploadFixedMeasurementsService? {
        val authToken = authenticationHelper.getAuthToken() ?: return null

        val apiService = apiServiceFactory.get(authToken)
        return UploadFixedMeasurementsService(apiService, errorHandler)
    }

    @Provides
    fun providesSessionsSyncService(
        apiServiceFactory: ApiServiceFactory,
        settings: Settings,
        errorHandler: ErrorHandler,
        authenticationHelper: AuthenticationHelper
    ): SessionsSyncService? {
        val authToken = authenticationHelper.getAuthToken() ?: return null

        val apiService = apiServiceFactory.get(authToken)
        return SessionsSyncService.get(apiService, errorHandler, settings)
    }
}
