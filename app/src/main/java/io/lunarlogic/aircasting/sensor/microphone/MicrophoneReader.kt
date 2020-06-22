package io.lunarlogic.aircasting.sensor.microphone

import io.lunarlogic.aircasting.events.AudioReaderErrorEvent
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import org.greenrobot.eventbus.EventBus

class MicrophoneReader: AudioReader.Listener() {
    private val SAMPLE_RATE = 44100
    private val SYMBOL = "dB"
    private val UNIT = "decibels"
    private val MEASUREMENT_TYPE = "Sound Level"
    private val SHORT_TYPE = "dB"
    private val SENSOR_NAME = "Phone Microphone"
    private val SENSOR_PACKAGE_NAME = deviceId

    companion object {
        val deviceId = "Builtin"
    }

    private val VERY_LOW = 20
    private val LOW = 60
    private val MID = 70
    private val HIGH = 80
    private val VERY_HIGH = 100

    private val audioReader = AudioReader()
    private val signalPower = SignalPower()
    private var calibrationHelper = CalibrationHelper()

    fun start() {
        // The AudioReader sleeps as much as it records
        val block = SAMPLE_RATE / 2

        audioReader.startReader(SAMPLE_RATE, block, this)
    }

    fun stop() {
        audioReader.stopReader()
    }

    override fun onReadComplete(buffer: ShortArray) {
        val power = signalPower.calculatePowerDb(buffer)
        if (power != null) {
            val calibrated = calibrationHelper.calibrate(power)
            val event = NewMeasurementEvent(
                SENSOR_PACKAGE_NAME, SENSOR_NAME, MEASUREMENT_TYPE, SHORT_TYPE, UNIT, SYMBOL,
                VERY_LOW, LOW, MID, HIGH, VERY_HIGH, calibrated
            )

            EventBus.getDefault().post(event)
        }
    }

    override fun onReadError(error: Int) {
        EventBus.getDefault().post(AudioReaderErrorEvent())
    }
}
