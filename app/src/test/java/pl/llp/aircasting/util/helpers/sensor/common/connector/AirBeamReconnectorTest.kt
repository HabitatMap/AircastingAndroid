package pl.llp.aircasting.util.helpers.sensor.common.connector

import com.appmattus.kotlinfixture.kotlinFixture
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import pl.llp.aircasting.data.model.AirbeamConnectionStatus

@OptIn(ExperimentalCoroutinesApi::class)
class AirBeamReconnectorTest {

    private val testScope = TestScope()

    @Test
    fun reconnect_whenConnectionStatusIsNull_doesNotCrash() = testScope.runTest {
        val connectionStatus = MutableStateFlow<AirbeamConnectionStatus?>(null)
        val syncStatus = MutableSharedFlow<Boolean>()
        val testSubject = AirBeamReconnector(
            mContext = mock(),
            mSessionsRepository = mock(),
            mAirBeamDiscoveryService = mock(),
            coroutineScope = testScope,
            sessionUuidByStandaloneMode = mock(),
            connectionStatusFlow = connectionStatus,
            syncStatusFlow = syncStatus,
        )
        testSubject.reconnect(
            session = kotlinFixture()(),
            deviceItem = null,
            errorCallback = null,
            finallyCallback = null,
        )
        syncStatus.emit(true)
        connectionStatus.emit(null)
        advanceUntilIdle()
        coroutineContext.cancelChildren()
    }
}