package pl.llp.aircasting.util.helpers.sensor.common

import android.util.Base64
import pl.llp.aircasting.util.TimezoneHelper
import javax.inject.Inject

class HexMessagesBuilder @Inject constructor() {
    private val BEGIN_MESSAGE_CODE = 0xfe.toByte()
    private val END_MESSAGE_CODE = 0xff.toByte()
    private val BLUETOOTH_STREAMING_METHOD_CODE = 0x01.toByte()
    private val WIFI_CODE = 0x02.toByte()
    private val CELLULAR_CODE = 0x03.toByte()
    private val UUID_CODE = 0x04.toByte()
    private val AUTH_TOKEN_CODE = 0x05.toByte()
    private val LAT_LNG_CODE = 0x06.toByte()
    private val CURRENT_TIME_CODE = 0x08.toByte()
    private val SYNC_CODE = 0x09.toByte()
    private val CLEAR_SD_CARD_CODE = 0x0a.toByte()

    private val BLUETOOTH_CONFIGURATION_MESSAGE =
        byteArrayOf(BEGIN_MESSAGE_CODE, BLUETOOTH_STREAMING_METHOD_CODE, END_MESSAGE_CODE)
    private val CELLULAR_CONFIGURATION_MESSAGE =
        byteArrayOf(BEGIN_MESSAGE_CODE, CELLULAR_CODE, END_MESSAGE_CODE)
    private val SYNC_CONFIGURATION_MESSAGE =
        byteArrayOf(BEGIN_MESSAGE_CODE, SYNC_CODE, END_MESSAGE_CODE)
    private val CLEAR_SD_CARD_CONFIGURATION_MESSAGE =
        byteArrayOf(BEGIN_MESSAGE_CODE, CLEAR_SD_CARD_CODE, END_MESSAGE_CODE)

    val bluetoothConfigurationMessage get() = BLUETOOTH_CONFIGURATION_MESSAGE
    val cellularConfigurationMessage get() = CELLULAR_CONFIGURATION_MESSAGE
    val downloadFromSDCardConfigurationMessage get() = SYNC_CONFIGURATION_MESSAGE
    val clearSDCardConfigurationMessage get() = CLEAR_SD_CARD_CONFIGURATION_MESSAGE

    fun uuidMessage(uuid: String): ByteArray {
        return buildMessage(uuid, UUID_CODE)
    }

    fun authTokenMessage(authToken: String): ByteArray {
        val rawAuthToken = "$authToken:X"
        val encodedCredentials = Base64.encodeToString(rawAuthToken.toByteArray(), Base64.NO_WRAP)

        return buildMessage(encodedCredentials, AUTH_TOKEN_CODE)
    }

    fun locationMessage(lat: Double, lng: Double): ByteArray {
        val rawLatLngStr = "${lng},${lat}"
        return buildMessage(rawLatLngStr, LAT_LNG_CODE)
    }

    fun currentTimeMessage(dateString: String): ByteArray {
        return buildMessage(dateString, CURRENT_TIME_CODE)
    }

    fun wifiConfigurationMessage(wifiSSID: String, wifiPassword: String): ByteArray {
        val GMTOffset = TimezoneHelper.getTimezoneOffsetInHours()
        val rawWifiConfigStr = wifiSSID + "," + wifiPassword + "," + GMTOffset
        return buildMessage(rawWifiConfigStr, WIFI_CODE)
    }

    private fun buildMessage(messageString: String, configurationCode: Byte): ByteArray {
        val hexString = asciiToHex(messageString)
        val messageList = hexStringToByteList(hexString, configurationCode)

        return byteListToArray(messageList)
    }

    fun asciiToHex(asciiStr: String): String {
        val chars = asciiStr.toCharArray()
        val hex = StringBuilder()

        for (ch in chars) {
            hex.append(Integer.toHexString(ch.code))
        }

        return hex.toString()
    }

    fun hexStringToByteList(s: String, configurationCode: Byte): ArrayList<Byte> {
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

    fun byteListToArray(messageList: ArrayList<Byte>): ByteArray {
        val message = ByteArray(messageList.size)

        for (i in messageList.indices) {
            message[i] = messageList[i]
        }

        return message
    }
}
