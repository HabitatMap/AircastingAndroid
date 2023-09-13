package pl.llp.aircasting.util.helpers.sensor.airbeam2

import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.helpers.sensor.HexMessagesBuilder
import java.io.OutputStream
import javax.inject.Inject

// TODO: Add to DI
class AirBeam2Configurator @Inject constructor(
    private val mSettings: Settings,
    private val mHexMessagesBuilder: HexMessagesBuilder,
) {

    fun sendAuth(sessionUUID: String, outputStream: OutputStream) {
        sendUUID(sessionUUID, outputStream)
        sleepFor(3000)
        sendAuthToken(mSettings.getAuthToken()!!, outputStream)
    }

    fun configure(
        session: Session,
        wifiSSID: String?,
        wifiPassword: String?,
        outputStream: OutputStream
    ) {
        when (session.type) {
            Session.Type.MOBILE -> configureMobileSession(outputStream)
            Session.Type.FIXED -> configureFixedSession(
                session,
                wifiSSID,
                wifiPassword,
                outputStream
            )
        }
    }

    fun reconnectMobileSession(outputStream: OutputStream) {
        configureMobileSession(outputStream)
    }

    private fun configureMobileSession(outputStream: OutputStream) {
        sendMessage(mHexMessagesBuilder.bluetoothConfigurationMessage, outputStream)
    }

    private fun configureFixedSession(
        session: Session,
        wifiSSID: String?,
        wifiPassword: String?,
        outputStream: OutputStream
    ) {
        val location = session.location
        val streamingMethod = session.streamingMethod

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
        streamingMethod: Session.StreamingMethod,
        wifiSSID: String?,
        wifiPassword: String?,
        outputStream: OutputStream
    ) {
        val streamingMethodMessage = when (streamingMethod) {
            Session.StreamingMethod.CELLULAR -> mHexMessagesBuilder.cellularConfigurationMessage
            Session.StreamingMethod.WIFI -> mHexMessagesBuilder.wifiConfigurationMessage(
                wifiSSID!!,
                wifiPassword!!
            )
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
        } catch (ignore: InterruptedException) {
        }
    }
}
