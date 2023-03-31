package pl.llp.aircasting.di.mocks

import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.helpers.stubDeviceItem
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.airbeam2.AirBeam2Connector
import pl.llp.aircasting.util.helpers.sensor.airbeam2.AirBeam2Reader

class FakeAirBeam2Connector(
    private val app: AircastingApplication,
    private val mSettings: Settings,
    private val mErrorHandler: ErrorHandler,
    bluetoothManager: BluetoothManager,
): AirBeam2Connector(mSettings, mErrorHandler, bluetoothManager) {
    private val mAirBeam2Reader = AirBeam2Reader(mErrorHandler)
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
                val inputStream = app.resources.openRawResource(R.raw.airbeam2_stream)
                mAirBeam2Reader.run(inputStream)
                sleep(1000)
                inputStream.close()
            }
        }
    }
}
