package io.lunarlogic.aircasting

import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer


class MockResponseRule(private val mockResponse: MockResponse) : TestRule {
    override fun apply(base: Statement, description: Description)
            = MyStatement(mockResponse, base)

    class MyStatement(private val mockResponse: MockResponse, private val base: Statement) : Statement() {
        @Throws(Throwable::class)
        override fun evaluate() {
            val mockWebServer = MockWebServer()
            mockWebServer.start()
            val baseUrl = mockWebServer.url("/")
            ApiServiceFactory.baseUrl = baseUrl

            mockWebServer.enqueue(mockResponse)

            try {
                base.evaluate() // This executes your tests
            } finally {
                mockWebServer.shutdown()
            }
        }
    }
}