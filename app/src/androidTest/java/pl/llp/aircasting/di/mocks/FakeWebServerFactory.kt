package pl.llp.aircasting.di.mocks

import pl.llp.aircasting.di.modules.WebServerFactory
import okhttp3.mockwebserver.MockWebServer

class FakeWebServerFactory: WebServerFactory() {
    fun getMockWebServer(): MockWebServer = MockWebServer()
}
