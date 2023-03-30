package pl.llp.aircasting.di

import dagger.Module
import dagger.Provides
import okhttp3.mockwebserver.MockWebServer
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.api.services.Authenticated
import javax.inject.Singleton

@Module
class TestApiModule {
    @Provides
    @Singleton
    fun providesServer(): MockWebServer = MockWebServer()

    @Provides
    @Authenticated
    fun provideApiService(factory: ApiServiceFactory): ApiService {
        return factory.getNonAuthenticated()
    }
}
