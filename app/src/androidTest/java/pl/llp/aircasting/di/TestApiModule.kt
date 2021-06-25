package pl.llp.aircasting.di

import pl.llp.aircasting.di.mocks.FakeApiServiceFactory
import pl.llp.aircasting.di.mocks.FakeWebServerFactory
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.networking.services.ApiServiceFactory

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
