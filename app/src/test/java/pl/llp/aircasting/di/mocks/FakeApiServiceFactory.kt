package pl.llp.aircasting.di.mocks

import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import pl.llp.aircasting.data.api.interceptor.NetworkConnectionInterceptor
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.util.Settings

class FakeApiServiceFactory(
    val mockWebServer: MockWebServer,
    settings: Settings,
    networkConnectionInterceptor: NetworkConnectionInterceptor,
) : ApiServiceFactory(settings, networkConnectionInterceptor) {
    override fun baseUrl(): HttpUrl {
        return mockWebServer.url("/")
    }
}
