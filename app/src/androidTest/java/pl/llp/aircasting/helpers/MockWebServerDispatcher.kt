package pl.llp.aircasting.helpers

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

object MockWebServerDispatcher {
    fun set(config: Map<String, MockResponse>, mockWebServer: MockWebServer) {
        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return config[request.path] ?: MockResponse().setResponseCode(404)
            }
        }
        mockWebServer.dispatcher = dispatcher
    }

    fun setNotFullPath(config: Map<String, MockResponse>, mockWebServer: MockWebServer) {
        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                config.entries.forEach {
                    val partialAddress = it.key
                    val response = it.value
                    if (request.path?.contains(partialAddress) == true)
                        return response
                }
                return MockResponse().setResponseCode(404)
            }
        }
        mockWebServer.dispatcher = dispatcher
    }
}
