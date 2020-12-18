package io.lunarlogic.aircasting.di.mocks

import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer

class FakeApiServiceFactory(mSettings: Settings, val mockWebServer: MockWebServer) : ApiServiceFactory(mSettings) {
    override fun baseUrl(): HttpUrl {
        return mockWebServer.url("/")
    }
}
