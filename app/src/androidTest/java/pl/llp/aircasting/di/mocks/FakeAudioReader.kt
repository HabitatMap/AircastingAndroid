package pl.llp.aircasting.di.mocks

import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.util.helpers.sensor.microphone.AudioReader
import java.nio.ByteBuffer
import java.nio.ByteOrder


class FakeAudioReader(private val app: AircastingApplication): AudioReader() {
    override fun startReader(rate: Int, block: Int, listener: Listener) {
        inputListener = listener
        val thread = Thread(Runnable { readerRun() }, "Audio Reader")
        thread.start()
    }
    override fun readerRun() {
        while (true) {
            val bytes = app.resources.openRawResource(R.raw.airbeam2_stream).readBytes()
            val shorts = ShortArray(bytes.size / 2)
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)

            readDone(shorts)
            Thread.sleep(1000)
        }
    }
}
