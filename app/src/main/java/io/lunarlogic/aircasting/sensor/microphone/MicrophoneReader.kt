package io.lunarlogic.aircasting.sensor.microphone

import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.exceptions.AudioReaderError
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.ResultCodes
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import org.greenrobot.eventbus.EventBus

class MicrophoneDeviceItem: DeviceItem() {
    companion object {
        val DEFAULT_ID = "Builtin"
    }

    override val id: String
        get() = DEFAULT_ID

    override val type: Type
        get() = Type.MIC
}

class MicrophoneReader(private val mAudioReader: AudioReader, private val mErrorHandler: ErrorHandler): AudioReader.Listener() {
    private val SAMPLE_RATE = 44100
    private val SYMBOL = "dB"
    private val UNIT = "decibels"
    private val MEASUREMENT_TYPE = "Sound Level"
    private val SHORT_TYPE = "dB"
    private val SENSOR_NAME = "Phone Microphone-dB"
    private val SENSOR_PACKAGE_NAME = MicrophoneDeviceItem.DEFAULT_ID

    private val VERY_LOW = 20
    private val LOW = 60
    private val MID = 70
    private val HIGH = 80
    private val VERY_HIGH = 100

    private val signalPower = SignalPower()
    private var calibrationHelper = CalibrationHelper()

    fun start() {
        println("MARYSIA: start microphone")
        // The AudioReader sleeps as much as it records
        val block = SAMPLE_RATE / 2

        mAudioReader.startReader(SAMPLE_RATE, block, this)
    }

    fun stop() {
        mAudioReader.stopReader()
    }

    override fun onReadComplete(buffer: ShortArray) {
        println("MARYSIA: onReadComplete")
        val power = signalPower.calculatePowerDb(buffer)
        println("MARYSIA: onReadComplete power: ${power}")
        if (power != null) {
            val calibrated = calibrationHelper.calibrate(power)
            val event = NewMeasurementEvent(
                SENSOR_PACKAGE_NAME, SENSOR_NAME, MEASUREMENT_TYPE, SHORT_TYPE, UNIT, SYMBOL,
                VERY_LOW, LOW, MID, HIGH, VERY_HIGH, calibrated
            )

            EventBus.getDefault().postSticky(event)
        } else {
            val event = NewMeasurementEvent(
                SENSOR_PACKAGE_NAME, SENSOR_NAME, MEASUREMENT_TYPE, SHORT_TYPE, UNIT, SYMBOL,
                VERY_LOW, LOW, MID, HIGH, VERY_HIGH, 10.0
            )
            EventBus.getDefault().postSticky(event)
        }
    }

    override fun onReadError(error: Int) {
        val message = mErrorHandler.obtainMessage(ResultCodes.AUDIO_READER_ERROR, AudioReaderError(error))
        message.sendToTarget()
    }
}
