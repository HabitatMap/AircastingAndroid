package pl.llp.aircasting.data.api.services

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import pl.llp.aircasting.util.Settings
import kotlin.test.assertEquals

class ApiServiceFactoryTest {
    @Test
    fun baseUrl_getsUrlFromSettings_constructsBaseUrlWithPort_hasSlashAtTheEnd() {
        val modifiedUrl = "https://aircasting.habitatmap.org"
        val port = "80"
        val settings = mock<Settings> {
            on { getBackendUrl() } doReturn modifiedUrl
            on { getBackendPort() } doReturn port
        }
        val factory = Factory(settings)

        val result = factory.baseUrl()

        verify(settings).getBackendUrl()
        verify(settings).getBackendPort()
        assertEquals("$modifiedUrl:$port".toHttpUrl(), result)
        assertEquals('/', result.toString().last())
    }
}

class Factory(settings: Settings) : ApiServiceFactory(settings) {
    public override fun baseUrl(): HttpUrl {
        return super.baseUrl()
    }
}
