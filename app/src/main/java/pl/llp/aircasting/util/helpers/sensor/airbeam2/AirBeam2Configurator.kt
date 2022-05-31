package pl.llp.aircasting.util.helpers.sensor.airbeam2

import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.helpers.sensor.HexMessagesBuilder
import java.io.OutputStream

class AirBeam2Configurator(private val mSettings: Settings) {
    private val mHexMessagesBuilder = HexMessagesBuilder()

    fun sendAuth(sessionUUID: String, outputStream: OutputStream) {
        sendUUID(sessionUUID, outputStream)
        sleepFor(3000)
        sendAuthToken(mSettings.getAuthToken()!!, outputStream)
    }

    fun configure(
        localSession: LocalSession,
        wifiSSID: String?,
        wifiPassword: String?,
        outputStream: OutputStream
    ){
        when(localSession.type) {
            LocalSession.Type.MOBILE -> configureMobileSession(outputStream)
            LocalSession.Type.FIXED -> configureFixedSession(localSession, wifiSSID, wifiPassword, outputStream)
        }
    }

    fun reconnectMobileSession(outputStream: OutputStream) {
        configureMobileSession(outputStream)
    }

    private fun configureMobileSession(outputStream: OutputStream) {
        sendMessage(mHexMessagesBuilder.bluetoothConfigurationMessage, outputStream)
    }

    private fun configureFixedSession(
        localSession: LocalSession,
        wifiSSID: String?,
        wifiPassword: String?,
        outputStream: OutputStream
    ) {
        val location = localSession.location
        val streamingMethod = localSession.streamingMethod

        location ?: return
        streamingMethod ?: return

        sleepFor(3000)
        configureLocation(location.latitude, location.longitude, outputStream)
        sleepFor(3000)
        configureStreamingMethod(streamingMethod, wifiSSID, wifiPassword, outputStream)
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
        streamingMethod: LocalSession.StreamingMethod,
        wifiSSID: String?,
        wifiPassword: String?,
        outputStream: OutputStream
    ) {
        val streamingMethodMessage = when (streamingMethod) {
            LocalSession.StreamingMethod.CELLULAR -> mHexMessagesBuilder.cellularConfigurationMessage
            LocalSession.StreamingMethod.WIFI -> mHexMessagesBuilder.wifiConfigurationMessage(wifiSSID!!, wifiPassword!!)
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
