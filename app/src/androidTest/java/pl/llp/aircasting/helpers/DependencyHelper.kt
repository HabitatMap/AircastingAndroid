package pl.llp.aircasting.helpers

import pl.llp.aircasting.di.mocks.FakeApiServiceFactory
import pl.llp.aircasting.networking.services.ApiServiceFactory
import okhttp3.mockwebserver.MockWebServer

fun getFakeApiServiceFactoryFrom(apiServiceFactory: ApiServiceFactory) : FakeApiServiceFactory {
    return (apiServiceFactory as FakeApiServiceFactory)
}

fun getMockWebServerFrom(apiServiceFactory: ApiServiceFactory) : MockWebServer {
    return getFakeApiServiceFactoryFrom(apiServiceFactory).mockWebServer
}
