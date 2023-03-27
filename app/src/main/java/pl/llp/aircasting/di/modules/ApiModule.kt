package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.data.api.services.*
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
    @Authenticated
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
}
