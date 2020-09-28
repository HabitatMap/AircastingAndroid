package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.exceptions.DownloadMeasurementsError
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.networking.responses.SessionStreamWithMeasurementsResponse
import io.lunarlogic.aircasting.networking.responses.SessionWithMeasurementsResponse
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DownloadMeasurementsCallback(
    private val sessionId: Long,
    private val session: Session,
    private val sessionsRepository: SessionsRepository,
    private val measurementStreamsRepository: MeasurementStreamsRepository,
    private val measurementsRepository: MeasurementsRepository,
    private val errorHandler: ErrorHandler
): Callback<SessionWithMeasurementsResponse> {
    override fun onResponse(
        call: Call<SessionWithMeasurementsResponse>,
        response: Response<SessionWithMeasurementsResponse>
    ) {
        if (response.isSuccessful) {
            val body = response.body()
            body?.streams?.let { streams ->
                DatabaseProvider.runQuery {
                    val streamResponses = streams.values
                    streamResponses.forEach { streamResponse ->
                        saveStreamData(streamResponse)
                    }
                }
            }
        } else {
            errorHandler.handleAndDisplay(DownloadMeasurementsError())
        }
    }

    override fun onFailure(
        call: Call<SessionWithMeasurementsResponse>,
        t: Throwable
    ) {
        errorHandler.handleAndDisplay(DownloadMeasurementsError(t))
    }

    private fun saveStreamData(streamResponse: SessionStreamWithMeasurementsResponse) {
        val stream = MeasurementStream(streamResponse)
        val streamId = measurementStreamsRepository.getIdOrInsert(
            sessionId,
            stream
        )
        streamResponse.measurements.forEach { measurementResponse ->
            val measurement = Measurement(measurementResponse)
            measurementsRepository.insert(
                streamId,
                sessionId,
                measurement
            )
            sessionsRepository.update(session)
        }
    }
}
