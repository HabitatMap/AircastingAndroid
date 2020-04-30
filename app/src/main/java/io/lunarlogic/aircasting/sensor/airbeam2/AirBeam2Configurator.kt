package io.lunarlogic.aircasting.sensor.airbeam2

class AirBeam2Configurator {
    private val BEGIN_MESSAGE_CODE = 0xfe.toByte()
    private val END_MESSAGE_CODE = 0xff.toByte()
    private val BLUETOOTH_STREAMING_METHOD_CODE = 0x01.toByte()

    private val BLUETOOTH_CONFIGURATION_MESSAGE =
        byteArrayOf(BEGIN_MESSAGE_CODE, BLUETOOTH_STREAMING_METHOD_CODE, END_MESSAGE_CODE)


    fun configureBluetooth() {
        sendMessage(BLUETOOTH_CONFIGURATION_MESSAGE)
    }

    fun sendMessage(bytes: ByteArray) {

    }
}