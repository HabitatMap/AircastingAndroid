package pl.llp.aircasting

import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before

abstract class BaseTest {
    lateinit var server: MockWebServer

    @Before
    open fun setup() {
        server.start()
    }

    @After
    open fun cleanup() {
        server.shutdown()
    }
}
