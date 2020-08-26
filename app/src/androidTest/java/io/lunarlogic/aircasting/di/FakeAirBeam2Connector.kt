package io.lunarlogic.aircasting.di

import androidx.test.espresso.idling.CountingIdlingResource
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Configurator
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Connector
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Reader
import java.util.concurrent.atomic.AtomicBoolean

class FakeAirBeam2Connector(
    private val app: AircastingApplication,
    errorHandler: ErrorHandler,
    private val mAirBeamConfigurator: AirBeam2Configurator,
    private val mAirBeam2Reader: AirBeam2Reader,
    private val mIdlingResource: CountingIdlingResource
): AirBeam2Connector(errorHandler, mAirBeamConfigurator, mAirBeam2Reader) {
    private val connectionStarted = AtomicBoolean(false)
    private var mThread: ConnectThread? = null

    override fun connect(deviceItem: DeviceItem) {
        if (connectionStarted.get() == false) {
            connectionStarted.set(true)
            mThread = ConnectThread(deviceItem)
            mThread?.start()
        }
    }

    private inner class ConnectThread(private val deviceItem: DeviceItem) : Thread() {
        override fun run() {
            mIdlingResource.increment()
            sleep(2000) // imitate connection time

            listener.onConnectionSuccessful(deviceItem.id)
            mIdlingResource.decrement()

            while (true) {
                val inputStream = app.resources.openRawResource(R.raw.airbeam2_stream)
                mAirBeam2Reader.run(inputStream)
                sleep(1000)
                inputStream.close()
            }
        }
    }
}
