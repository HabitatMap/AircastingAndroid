package pl.llp.aircasting.helpers

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

class MockWebServerDispatcher {
    companion object {
        fun set(config: Map<String, MockResponse>, mockWebServer: MockWebServer) {
            val dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    return config[request.path] ?: MockResponse().setResponseCode(404)
                }
            }
            mockWebServer.dispatcher = dispatcher
        }
    }
}
