package pl.llp.aircasting.di.mocks

import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.airbeamNonSyncable.configurator.AirBeam2Configurator
import pl.llp.aircasting.util.helpers.sensor.airbeamNonSyncable.reader.AirBeam2Reader
import pl.llp.aircasting.util.helpers.sensor.airbeamNonSyncable.connector.AirBeam2Connector
import pl.llp.aircasting.utilities.stubDeviceItem

class FakeAirBeam2Connector(
    private val app: AircastingApplication,
    mErrorHandler: ErrorHandler,
    bluetoothManager: BluetoothManager,
    mAirBeamConfigurator: AirBeam2Configurator,
    private val mAirBeam2Reader: AirBeam2Reader,
) : AirBeam2Connector(
    mErrorHandler,
    bluetoothManager,
    mAirBeamConfigurator,
    mAirBeam2Reader
) {
    private var mThread: ConnectThread? = null

    override fun start(deviceItem: DeviceItem) {
        // Mockito seems to not work well with ForegroundServices so it's needed there again
        val deviceItem = stubDeviceItem()

        mThread = ConnectThread(deviceItem)
        mThread?.start()
    }

    private inner class ConnectThread(private val deviceItem: DeviceItem) : Thread() {
        override fun run() {
            onConnectionSuccessful(deviceItem)

            while (true) {
                val inputStream = this::class.java.classLoader!!.getResourceAsStream("airbeam2_stream")
                mAirBeam2Reader.run(inputStream)
                sleep(1000)
                inputStream.close()
            }
        }
    }
}