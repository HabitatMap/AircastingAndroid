package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.api.services.Authenticated
import pl.llp.aircasting.data.api.services.NonAuthenticated
import pl.llp.aircasting.util.Settings
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
}
