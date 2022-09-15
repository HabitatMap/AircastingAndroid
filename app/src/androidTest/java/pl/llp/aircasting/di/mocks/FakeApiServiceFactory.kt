package pl.llp.aircasting.di.mocks

import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import pl.llp.aircasting.data.api.services.ApiServiceFactory

class FakeApiServiceFactory(val mockWebServer: MockWebServer) : ApiServiceFactory() {
    override fun baseUrl(): HttpUrl {
        return mockWebServer.url("/")
    }
}
