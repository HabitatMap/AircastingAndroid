package pl.llp.aircasting.data.api.services

import android.util.Log
import com.google.gson.Gson
import pl.llp.aircasting.data.api.GzippedParams
import pl.llp.aircasting.data.api.params.CreateFixedSessionV3Body
import pl.llp.aircasting.data.api.params.CreateSessionBody
import pl.llp.aircasting.data.api.params.SessionParams
import pl.llp.aircasting.data.api.response.UploadSessionResponse
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.UnexpectedAPIError
import java.util.Date
import javax.inject.Inject

/**
 * Returned by [FixedSessionUploader] for V2 fixed sessions.
 * Contains the data needed to build the NewSessionConfig BLE payload.
 *
 * @param location  URL returned by the backend (may be null on failure).
 * @param sessionToken  16-byte session token decoded from the backend's 32-char hex string.
 * @param sensorTypeIds  Map of sensor_name → sensor_type_id from backend streams response.
 */
data class FixedSessionConfig(
    val location: String?,
    val sessionToken: ByteArray,
    val sensorTypeIds: Map<String, Int>,
)

interface FixedSessionUploader {
    suspend operator fun invoke(session: Session, isV2: Boolean = false): FixedSessionConfig?
}

@UserSessionScope
class FixedSessionUploaderDefault @Inject constructor(
    @Authenticated private val apiService: ApiService,
    private val errorHandler: ErrorHandler,
    private val sessionsRepository: SessionsRepository,
) : FixedSessionUploader {

    override suspend fun invoke(session: Session, isV2: Boolean): FixedSessionConfig? {
        return runCatching {
            session.endTime = Date()

            if (isV2) {
                return@runCatching invokeV2(session)
            }

            val sessionParams = SessionParams(session)
            val sessionBody = CreateSessionBody(GzippedParams.get(sessionParams, SessionParams::class.java))
            val response = apiService.createFixedSession(sessionBody)

            if (!response.isSuccessful) {
                throw UnexpectedAPIError()
            }

            val body = response.body()
            sessionsRepository.updateUrlLocation(session, body?.location)

            val tokenHex = body?.session_token
            val streams = body?.streams

            Log.d(TAG, "FixedSessionUploader: response body: location=${body?.location}, session_token=${tokenHex}, streams=${streams}")

            if (tokenHex != null && streams != null) {
                // Backend stores session_token as a 16-byte integer, returned as a 32-char hex string.
                val tokenBytes = tokenHex.chunked(2)
                    .map { it.toInt(16).toByte() }
                    .toByteArray()
                val sensorTypeIds = streams.associate { it.sensor_name to it.sensor_type_id }
                Log.d(TAG, "FixedSessionUploader: parsed token (${tokenBytes.size}B), sensorTypeIds=$sensorTypeIds")
                FixedSessionConfig(
                    location = body.location,
                    sessionToken = tokenBytes,
                    sensorTypeIds = sensorTypeIds,
                )
            } else {
                Log.e(TAG, "FixedSessionUploader: backend response missing session_token or streams — cannot configure V2 fixed session. token=$tokenHex streams=$streams")
                null
            }
        }.onFailure { throwable ->
            Log.e(TAG, "FixedSessionUploader: API call failed", throwable)
            errorHandler.handle(UnexpectedAPIError(throwable))
        }.getOrNull()
    }

    private suspend fun invokeV2(session: Session): FixedSessionConfig? {
        val requestBody = buildV3Body(session)
        Log.d(TAG, "FixedSessionUploader: calling /api/v3/fixed_sessions for V2 device, body=$requestBody")
        val response = apiService.createFixedSessionV3(requestBody)
        val rawBody = response.body()?.string() ?: response.errorBody()?.string() ?: ""
        Log.d(TAG, "FixedSessionUploader: V3 HTTP ${response.code()} raw body: $rawBody")

        if (!response.isSuccessful) {
            Log.e(TAG, "FixedSessionUploader: V3 endpoint returned error ${response.code()}: $rawBody")
            throw UnexpectedAPIError()
        }

        val parsed = runCatching { Gson().fromJson(rawBody, UploadSessionResponse::class.java) }
            .onFailure { Log.e(TAG, "FixedSessionUploader: failed to parse V3 response as JSON: $rawBody", it) }
            .getOrNull()

        sessionsRepository.updateUrlLocation(session, parsed?.location)

        val tokenHex = parsed?.session_token
        val streams = parsed?.streams
        Log.d(TAG, "FixedSessionUploader: V3 parsed: location=${parsed?.location}, token=$tokenHex, streams=$streams")

        if (tokenHex == null || streams == null) {
            Log.e(TAG, "FixedSessionUploader: V3 response missing session_token or streams")
            return null
        }

        val tokenBytes = tokenHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val sensorTypeIds = streams.associate { it.sensor_name to it.sensor_type_id }
        Log.d(TAG, "FixedSessionUploader: V3 token (${tokenBytes.size}B), sensorTypeIds=$sensorTypeIds")
        return FixedSessionConfig(location = parsed.location, sessionToken = tokenBytes, sensorTypeIds = sensorTypeIds)
    }

    private fun buildV3Body(session: Session): CreateFixedSessionV3Body {
        val streams = listOf(
            CreateFixedSessionV3Body.StreamInfo("AirBeamMini-PM1", "µg/m³"),
            CreateFixedSessionV3Body.StreamInfo("AirBeamMini-PM2.5", "µg/m³"),
        )
        return CreateFixedSessionV3Body(
            uuid = session.uuid,
            title = session.name,
            latitude = session.location?.latitude,
            longitude = session.location?.longitude,
            contribute = session.contribute,
            is_indoor = session.indoor,
            airbeam = CreateFixedSessionV3Body.AirbeamInfo(
                mac_address = session.deviceId,
                model = "AirBeamMini",
                name = session.name,
            ),
            streams = streams,
        )
    }
}
