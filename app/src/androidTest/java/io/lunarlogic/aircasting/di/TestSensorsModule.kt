package io.lunarlogic.aircasting.di

import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Connector
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Reader
import java.util.concurrent.atomic.AtomicBoolean

class FakeAirBeam2Connector(private val app: AircastingApplication, errorHandler: ErrorHandler): AirBeam2Connector(errorHandler) {
    private val connectionStarted = AtomicBoolean(false)
    private var mThread: ConnectThread? = null
    private val mAirBeam2Reader = AirBeam2Reader()

    override fun connect(deviceItem: DeviceItem) {
        if (connectionStarted.get() == false) {
            connectionStarted.set(true)
            mThread = ConnectThread(deviceItem)
            mThread?.start()
        }
    }

    private inner class ConnectThread(private val deviceItem: DeviceItem) : Thread() {
        override fun run() {
            listener.onConnectionSuccessful(deviceItem.id)

            while (true) {
                val inputStream = app.resources.openRawResource(R.raw.airbeam2_stream)
                mAirBeam2Reader.run(inputStream)
                sleep(1000)
                inputStream.close()
            }
        }
    }
}

class TestSensorsModule(private val app: AircastingApplication): SensorsModule() {
    override fun providesAirbeam2Connector(errorHandler: ErrorHandler): AirBeam2Connector {
        return FakeAirBeam2Connector(app, errorHandler)
    }
}