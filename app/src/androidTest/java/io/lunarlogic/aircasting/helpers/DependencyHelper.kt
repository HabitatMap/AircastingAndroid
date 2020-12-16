package io.lunarlogic.aircasting.helpers

import io.lunarlogic.aircasting.di.mocks.FakeApiServiceFactory
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import okhttp3.mockwebserver.MockWebServer

fun getFakeApiServiceFactoryFrom(apiServiceFactory: ApiServiceFactory) : FakeApiServiceFactory {
    return (apiServiceFactory as FakeApiServiceFactory)
}

fun getMockWebServerFrom(apiServiceFactory: ApiServiceFactory) : MockWebServer {
    return getFakeApiServiceFactoryFrom(apiServiceFactory).mockWebServer
}
