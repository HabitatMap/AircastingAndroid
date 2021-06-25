package pl.llp.aircasting.di.mocks

import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.networking.services.ApiServiceFactory
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer

class FakeApiServiceFactory(mSettings: Settings, val mockWebServer: MockWebServer) : ApiServiceFactory(mSettings) {
    override fun baseUrl(): HttpUrl {
        return mockWebServer.url("/")
    }
}
