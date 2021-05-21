package io.lunarlogic.aircasting.sensor.airbeam2

import android.content.Context
import io.lunarlogic.aircasting.lib.AuthenticationHelper
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.sensor.HexMessagesBuilder
import java.io.OutputStream

class AirBeam2Configurator(private val mContext: Context,
                           private val mSettings: Settings) {
    private val mHexMessagesBuilder = HexMessagesBuilder()
    private val authenticationHelper = AuthenticationHelper(mContext) // todo: this one is a bit yolo, maybe i should inject this one everywhere <?>

    fun sendAuth(sessionUUID: String, outputStream: OutputStream) {
        sendUUID(sessionUUID, outputStream)
        sleepFor(3000)
        sendAuthToken(authenticationHelper.getAuthToken()!!, outputStream)
    }

    fun configure(
        session: Session,
        wifiSSID: String?,
        wifiPassword: String?,
        outputStream: OutputStream
    ){
        when(session.type) {
            Session.Type.MOBILE -> configureMobileSession(outputStream)
            Session.Type.FIXED -> configureFixedSession(session, wifiSSID, wifiPassword, outputStream)
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
