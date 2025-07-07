package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.connector

import android.bluetooth.BluetoothDevice
import android.content.Context
import io.mockk.every
import io.mockk.mockkStatic
import no.nordicsemi.android.ble.observer.ConnectionObserver.REASON_LINK_LOSS
import no.nordicsemi.android.ble.observer.ConnectionObserver.REASON_TERMINATE_PEER_USER
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import pl.llp.aircasting.util.events.SensorDisconnectedUnexpectedlyEvent
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.SyncableAirBeamConfigurator

class SyncableAirBeamConnectorTest {
    private lateinit var applicationContext: Context
    private lateinit var mErrorHandler: ErrorHandler
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var airBeam3Configurator: SyncableAirBeamConfigurator
    private lateinit var eventBus: EventBus

    @Before
    fun setup() {
        applicationContext = mock()
        mErrorHandler = mock()
        bluetoothManager = mock()
        airBeam3Configurator = mock()
        eventBus = mock<EventBus>()

        mockkStatic(EventBus::class)
        every { EventBus.getDefault() } returns eventBus
    }

    @Test
    fun onDeviceDisconnected_whenDisconnectedReasonIs_REASON_TERMINATE_PEER_USER_doesNotPostEvent() {
        val deviceId = "1234"
        val deviceName = "airbeam3:$deviceId"
        val device = mock<BluetoothDevice> {
            on(it.name) doReturn deviceName
        }
        val reason = REASON_TERMINATE_PEER_USER
        val instance = SyncableAirBeamConnector(
            applicationContext,
            mErrorHandler,
            bluetoothManager,
            airBeam3Configurator,
        )

        instance.onDeviceDisconnected(device, reason)

        verify(eventBus, times(0)).post(any<SensorDisconnectedUnexpectedlyEvent>())
    }

    @Test
    fun onDeviceDisconnected_whenDisconnectedReasonIsNOT_REASON_TERMINATE_PEER_USER_postsEvent() {
        val deviceId = "1234"
        val deviceName = "airbeam3:$deviceId"
        val device = mock<BluetoothDevice> {
            on(it.name) doReturn deviceName
        }
        val reason = REASON_LINK_LOSS
        val instance = SyncableAirBeamConnector(
            applicationContext,
            mErrorHandler,
            bluetoothManager,
            airBeam3Configurator,
        )

        instance.onDeviceDisconnected(device, reason)

        verify(eventBus).post(any<SensorDisconnectedUnexpectedlyEvent>())
    }
}