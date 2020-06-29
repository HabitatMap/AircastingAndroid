package io.lunarlogic.aircasting.sensor.airbeam2

import io.lunarlogic.aircasting.lib.Settings
import java.io.OutputStream
import io.lunarlogic.aircasting.sensor.Session

class AirBeam2Configurator(private val mSettings: Settings) {
    private val mHexMessagesBuilder = HexMessagesBuilder()

    fun configureSessionType(session: Session, outputStream: OutputStream) {
        println("ANIA configureSessionType " + session.type.toString())
        when(session.type) {
            Session.Type.MOBILE -> configureMobileSession(outputStream)
            Session.Type.FIXED -> configureFixedSession(session, outputStream)
        }
    }

    fun configureFixedSessionDetails(
        location: Session.Location,
        streamingMethod: Session.StreamingMethod,
        wifiSSID: String?,
        wifiPassword: String?,
        outputStream: OutputStream
    ) {
        println("ANIA configureFixedSessionDetails " + location.latitude + ", " + location.longitude + " " + wifiSSID + ", " + wifiPassword)
        configureLocation(location.latitude, location.longitude, outputStream)
        sleepFor(3000)
        configureStreamingMethod(streamingMethod, wifiSSID, wifiPassword, outputStream)
    }

    private fun configureMobileSession(outputStream: OutputStream) {
        sendMessage(mHexMessagesBuilder.bluetoothConfigurationMessage, outputStream)
    }

    private fun configureFixedSession(session: Session, outputStream: OutputStream) {
        println("ANIA configureFixedSession uuid " + session.uuid)
        println("ANIA configureFixedSession authToken " + mSettings.getAuthToken())
        sendUUID(session.uuid, outputStream)
        sleepFor(3000)
        sendAuthToken(mSettings.getAuthToken()!!, outputStream)
    }

    private fun sendUUID(uuid: String, outputStream: OutputStream) {
        val uuidMessage = mHexMessagesBuilder.uuidMessage(uuid)
        sendMessage(uuidMessage, outputStream)
    }

    private fun sendAuthToken(authToken: String, outputStream: OutputStream) {
        val authTokenMessage = mHexMessagesBuilder.authTokenMessage(authToken)
        sendMessage(authTokenMessage, outputStream)
    }

    private fun configureLocation(lat: Double, lng: Double, outputStream: OutputStream) {
        val locationMessage = mHexMessagesBuilder.locationMessage(lat, lng)
        sendMessage(locationMessage, outputStream)
    }

    private fun configureStreamingMethod(
        streamingMethod: Session.StreamingMethod,
        wifiSSID: String?,
        wifiPassword: String?,
        outputStream: OutputStream
    ) {
        val streamingMethodMessage = when (streamingMethod) {
            Session.StreamingMethod.CELLULAR -> mHexMessagesBuilder.cellularConfigurationMessage
            Session.StreamingMethod.WIFI -> mHexMessagesBuilder.wifiConfigurationMessage(wifiSSID!!, wifiPassword!!)
        }

        sendMessage(streamingMethodMessage, outputStream)
    }

    private fun sendMessage(bytes: ByteArray, outputStream: OutputStream) {
        outputStream.write(bytes)
        outputStream.flush()
    }

    private fun sleepFor(sleepTime: Long) {
        try {
            Thread.sleep(sleepTime)
        } catch (ignore: InterruptedException) {}
    }
}