package io.lunarlogic.aircasting.di.mocks

import io.lunarlogic.aircasting.di.WebServerFactory
import okhttp3.mockwebserver.MockWebServer

class FakeWebServerFactory: WebServerFactory() {
    fun getMockWebServer(): MockWebServer = MockWebServer()
}
