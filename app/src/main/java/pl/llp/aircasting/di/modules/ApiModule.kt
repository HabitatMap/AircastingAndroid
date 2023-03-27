package pl.llp.aircasting.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import pl.llp.aircasting.data.api.services.*
import pl.llp.aircasting.data.local.LogoutService
import pl.llp.aircasting.ui.view.screens.login.LoginService
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import javax.annotation.Nullable
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
    @AuthenticatedWithToken
    fun providesApiServiceAuthenticatedWithToken(
        settings: Settings,
        factory: ApiServiceFactory
    ): ApiService {
        return factory.getAuthenticated(settings.getAuthToken())
    }

    @Provides
    @Singleton
    @NonAuthenticated
    fun provideApiServiceNonAuthenticated(factory: ApiServiceFactory): ApiService {
        return factory.getNonAuthenticated()
    }

    @Provides
    @Singleton
    fun providesUploadFixedMeasurementsService(
        apiServiceFactory: ApiServiceFactory,
        settings: Settings,
        errorHandler: ErrorHandler
    ): UploadFixedMeasurementsService? {
        val authToken = settings.getAuthToken() ?: return null

        val apiService = apiServiceFactory.getAuthenticated(authToken)
        return UploadFixedMeasurementsService(apiService, errorHandler)
    }

    @Nullable
    @Provides
    fun providesSessionsSyncService(
        apiServiceFactory: ApiServiceFactory,
        settings: Settings,
        errorHandler: ErrorHandler
    ): SessionsSyncService? {
        val authToken = settings.getAuthToken() ?: return null

        val apiService = apiServiceFactory.getAuthenticated(authToken)
        return SessionsSyncService.get(apiService, errorHandler)
    }

    @Provides
    @Singleton
    @Nullable
    fun providesSessionDownloadService(
        apiServiceFactory: ApiServiceFactory,
        settings: Settings,
        errorHandler: ErrorHandler
    ): SessionDownloadService? {
        val authToken = settings.getAuthToken() ?: return null

        val apiService = apiServiceFactory.getAuthenticated(authToken)
        return SessionDownloadService(apiService, errorHandler)
    }

    @Provides
    @Singleton
    open fun providesLogoutService(
        settings: Settings,
        appContext: Context,
        apiServiceFactory: ApiServiceFactory,
        errorHandler: ErrorHandler
    ): LogoutService = LogoutService(settings, appContext, apiServiceFactory, errorHandler)

    @Provides
    @Singleton
    open fun providesLoginService(
        settings: Settings,
        errorHandler: ErrorHandler,
        apiServiceFactory: ApiServiceFactory
    ): LoginService = LoginService(settings, errorHandler, apiServiceFactory)
}
