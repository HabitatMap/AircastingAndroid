package io.lunarlogic.aircasting.sensor.airbeam2

import java.io.OutputStream
import java.io.UnsupportedEncodingException
import io.lunarlogic.aircasting.lib.base64.Base64
import java.util.*
import kotlin.collections.ArrayList

class AirBeam2Configurator {
    private val BEGIN_MESSAGE_CODE = 0xfe.toByte()
    private val END_MESSAGE_CODE = 0xff.toByte()
    private val BLUETOOTH_STREAMING_METHOD_CODE = 0x01.toByte()
    private val WIFI_CODE = 0x02.toByte()
    private val CELLULAR_CODE = 0x03.toByte()
    private val UUID_CODE = 0x04.toByte()
    private val AUTH_TOKEN_CODE = 0x05.toByte()
    private val LAT_LNG_CODE = 0x06.toByte()

    private val BLUETOOTH_CONFIGURATION_MESSAGE =
        byteArrayOf(BEGIN_MESSAGE_CODE, BLUETOOTH_STREAMING_METHOD_CODE, END_MESSAGE_CODE)
    private val CELLULAR_CONFIGURATION_MESSAGE =
        byteArrayOf(BEGIN_MESSAGE_CODE, CELLULAR_CODE, END_MESSAGE_CODE)


    fun configureBluetooth(outputStream: OutputStream) {
        sendMessage(BLUETOOTH_CONFIGURATION_MESSAGE, outputStream)
    }

    fun sendUUID(uuid: String, outputStream: OutputStream) {
        val uuidMessage = buildMessage(uuid, UUID_CODE)

        sendMessage(uuidMessage, outputStream)
    }

    fun sendAuthToken(authToken: String, outputStream: OutputStream) {
        var data = ByteArray(0)

        val rawAuthToken = "$authToken:X"

        try {
            data = rawAuthToken.toByteArray(charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
        }

        val base64 = Base64.encodeBase64String(data);

        val authTokenMessage = buildMessage(base64, AUTH_TOKEN_CODE)

        sendMessage(authTokenMessage, outputStream)
    }

    private fun buildMessage(messageString: String, configurationCode: Byte): ByteArray {
        val hexString = asciiToHex(messageString)
        val messageList = hexStringToByteList(hexString, configurationCode)

        return byteListToArray(messageList)
    }

    private fun asciiToHex(asciiStr: String): String {
        val chars = asciiStr.toCharArray()
        val hex = StringBuilder()

        for (ch in chars) {
            hex.append(Integer.toHexString(ch.toInt()))
        }

        return hex.toString()
    }

    private fun hexStringToByteList(s: String, configurationCode: Byte): ArrayList<Byte> {
        val len = s.length
        val message = ArrayList<Byte>()
        message.add(BEGIN_MESSAGE_CODE)
        message.add(configurationCode)

        var i = 0
        while (i < len) {
            message.add(
                ((Character.digit(s[i], 16) shl 4) + Character.digit(
                    s[i + 1],
                    16
                )).toByte()
            )
            i += 2
        }

        message.add(END_MESSAGE_CODE)

        return message
    }

    private fun byteListToArray(messageList: ArrayList<Byte>): ByteArray {
        val message = ByteArray(messageList.size)

        for (i in messageList.indices) {
            message[i] = messageList[i]
        }

        return message
    }

    private fun sendMessage(bytes: ByteArray, outputStream: OutputStream) {
        outputStream.write(bytes)
        outputStream.flush()
    }
}