package io.lunarlogic.aircasting.di

import io.lunarlogic.aircasting.di.mocks.FakeApiServiceFactory
import io.lunarlogic.aircasting.di.mocks.FakeWebServerFactory
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory

class TestApiModule() : ApiModule(){
    override fun providesMockWebServerFactory(): WebServerFactory =
        FakeWebServerFactory()

    override fun providesApiServiceFactory(settings: Settings, webServerFactory: WebServerFactory): ApiServiceFactory {
        val mockWebServer = (webServerFactory as FakeWebServerFactory).getMockWebServer()
        return FakeApiServiceFactory(
            settings,
            mockWebServer
        )
    }
}
