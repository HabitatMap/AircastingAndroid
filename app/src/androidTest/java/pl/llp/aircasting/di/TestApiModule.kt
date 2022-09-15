package pl.llp.aircasting.di

import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.di.mocks.FakeApiServiceFactory
import pl.llp.aircasting.di.mocks.FakeWebServerFactory
import pl.llp.aircasting.di.modules.ApiModule
import pl.llp.aircasting.di.modules.WebServerFactory
import pl.llp.aircasting.util.Settings

class TestApiModule : ApiModule(){
    override fun providesMockWebServerFactory(): WebServerFactory =
        FakeWebServerFactory()

    override fun providesApiServiceFactory(settings: Settings, webServerFactory: WebServerFactory): ApiServiceFactory {
        val mockWebServer = (webServerFactory as FakeWebServerFactory).getMockWebServer()
        return FakeApiServiceFactory(
            mockWebServer
        )
    }
}
