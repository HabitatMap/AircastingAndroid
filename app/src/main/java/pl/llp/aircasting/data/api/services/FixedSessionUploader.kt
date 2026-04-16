package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.GzippedParams
import pl.llp.aircasting.data.api.params.CreateSessionBody
import pl.llp.aircasting.data.api.params.SessionParams
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
 * @param sessionToken  16-byte session token decoded from the backend response.
 *   TODO: Confirm exact token format from backend — previously 128 bytes was considered.
 *         If the backend returns a 32-char hex string, decoding to 16 bytes is correct.
 *         If it returns raw bytes or a different length, this needs to change.
 * @param sensorTypeIds  Map of sensor_name → sensor_type_id from backend streams response.
 */
data class FixedSessionConfig(
    val location: String?,
    val sessionToken: ByteArray,
    val sensorTypeIds: Map<String, Int>,
)

fun interface FixedSessionUploader {
    suspend operator fun invoke(session: Session): FixedSessionConfig?
}

@UserSessionScope
class FixedSessionUploaderDefault @Inject constructor(
    @Authenticated private val apiService: ApiService,
    private val errorHandler: ErrorHandler,
    private val sessionsRepository: SessionsRepository,
) : FixedSessionUploader {

    override suspend fun invoke(session: Session): FixedSessionConfig? {
        return runCatching {
            session.endTime = Date()

            val sessionParams = SessionParams(session)
            val sessionBody =
                CreateSessionBody(GzippedParams.get(sessionParams, SessionParams::class.java))
            val response = apiService.createFixedSession(sessionBody)

            if (!response.isSuccessful) {
                throw UnexpectedAPIError()
            }

            val body = response.body()
            sessionsRepository.updateUrlLocation(session, body?.location)

            val tokenHex = body?.session_token
            val streams = body?.streams

            if (tokenHex != null && streams != null) {
                // TODO: Confirm token format with backend. Current assumption: 32-char hex → 16 bytes.
                // If the backend uses a different length or encoding, update this decoding logic.
                val tokenBytes = tokenHex.chunked(2)
                    .map { it.toInt(16).toByte() }
                    .toByteArray()
                val sensorTypeIds = streams.associate { it.sensor_name to it.sensor_type_id }
                FixedSessionConfig(
                    location = body.location,
                    sessionToken = tokenBytes,
                    sensorTypeIds = sensorTypeIds,
                )
            } else {
                null
            }
        }.onFailure { throwable ->
            errorHandler.handle(UnexpectedAPIError(throwable))
        }.getOrNull()
    }
}
