package io.lunarlogic.aircasting.di

import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer

class TestMockWebServerFactory: MockWebServerFactory() {
    fun getMockWebServer(): MockWebServer = MockWebServer()
}

class FakeApiServiceFactory(mSettings: Settings, val mockWebServer: MockWebServer) : ApiServiceFactory(mSettings) {
    override fun baseUrl(): HttpUrl {
        println("ANIA")
        return mockWebServer.url("/")
    }

}

class FakeApiServiceFactoryModule() : ApiModule(){
    override fun providesMockWebServerFactory(): MockWebServerFactory = TestMockWebServerFactory()

    override fun providesApiServiceFactory(settings: Settings, mockWebServerFactory: MockWebServerFactory): ApiServiceFactory {
        val mockWebServer = (mockWebServerFactory as TestMockWebServerFactory).getMockWebServer()
        return FakeApiServiceFactory(settings, mockWebServer)
    }
}
