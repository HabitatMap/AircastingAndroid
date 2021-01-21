package io.lunarlogic.aircasting.di.mocks

import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.helpers.stubDeviceItem
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Connector
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Reader

class FakeAirBeam2Connector(
    private val app: AircastingApplication,
    private val mSettings: Settings,
    private val mErrorHandler: ErrorHandler
): AirBeam2Connector(mSettings, mErrorHandler) {
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
            sleep(2000) // imitate connection time

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
