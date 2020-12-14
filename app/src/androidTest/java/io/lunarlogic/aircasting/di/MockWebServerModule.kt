package io.lunarlogic.aircasting.di

import dagger.Module
import dagger.Provides
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import okhttp3.mockwebserver.MockWebServer
import java.lang.NullPointerException
import javax.inject.Named
import javax.inject.Singleton

@Module
class MockWebServerModule {
    // TODO: mockWebServer initializaiton is not working ...
    @Provides
    @Singleton
    fun providesMockWebServer(): MockWebServer {
        return MockWebServer()
    }

}
