package io.lunarlogic.aircasting.di

import dagger.Module
import dagger.Provides
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import okhttp3.mockwebserver.MockWebServer
import javax.inject.Singleton

@Module
class MockWebServerModule {
    @Provides
    @Singleton
    fun providesMockWebServer(): MockWebServer {
        val mockWebServer = MockWebServer()
        val baseUrl = mockWebServer.url("/")
        ApiServiceFactory.baseUrl = baseUrl //TODO: has to be changed
        return mockWebServer
    }
}
