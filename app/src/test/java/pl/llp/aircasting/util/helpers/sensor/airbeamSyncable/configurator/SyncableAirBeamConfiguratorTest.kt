package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator

import android.os.Looper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import no.nordicsemi.android.ble.Request
import no.nordicsemi.android.ble.exception.RequestFailedException
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import pl.llp.aircasting.util.exceptions.AirBeam3ConfiguringFailed
import pl.llp.aircasting.util.exceptions.AirBeam3ConfiguringFailed.Type.SD_CARD
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.UnknownError


@OptIn(ExperimentalCoroutinesApi::class)
class SyncableAirBeamConfiguratorTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        mockkStatic(Looper::class)

        val fakeMainLooper = mockk<Looper>()
        val fakeCurrentLooper = mockk<Looper>()

        every { Looper.getMainLooper() } returns fakeMainLooper
        every { Looper.myLooper() } returns fakeCurrentLooper
    }

    @After
    fun tearDown() {
        unmockkStatic(Looper::class)
    }

    @Test
    fun clearSDCard_whenRequestFailed_callsErrorHandler() = runTest(testDispatcher) {
        val errorHandler = mock<ErrorHandler>()
        val request = mock<Request>()
        val status = 123
        val exception = RequestFailedException(request, status)
        val await = mock<RequestQueueCall> {
            on(it.invoke(any())) doAnswer { throw exception }
        }

        val syncableAirBeamConfigurator = AirBeam3Configurator(
            applicationContext = mock(),
            mErrorHandler = errorHandler,
            mSettings = mock(),
            hexMessagesBuilder = mock(),
            syncableAirBeamReader = mock(),
            sdCardReader = mock(),
            await = await,
        )

        syncableAirBeamConfigurator.clearSDCard()

        verify(errorHandler).handle(eq(AirBeam3ConfiguringFailed(SD_CARD, exception.status)))
    }

    @Test
    fun clearSDCard_whenUnknownExceptionOccurs_callsErrorHandler() = runTest(testDispatcher) {
        val errorHandler = mock<ErrorHandler>()
        val exception = InterruptedException()
        val await = mock<RequestQueueCall> {
            on(it.invoke(any())) doAnswer { throw exception }
        }

        val syncableAirBeamConfigurator = AirBeam3Configurator(
            applicationContext = mock(),
            mErrorHandler = errorHandler,
            mSettings = mock(),
            hexMessagesBuilder = mock(),
            syncableAirBeamReader = mock(),
            sdCardReader = mock(),
            await = await,
        )

        syncableAirBeamConfigurator.clearSDCard()

        verify(errorHandler).handle(eq(UnknownError(exception)))
    }
}