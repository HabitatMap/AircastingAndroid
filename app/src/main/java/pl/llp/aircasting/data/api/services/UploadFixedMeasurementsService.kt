package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.GzippedParams
import pl.llp.aircasting.data.api.params.UploadFixedMeasurementsBody
import pl.llp.aircasting.data.api.params.UploadFixedMeasurementsParams
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.UnexpectedAPIError
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.CSVMeasurement
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.CSVMeasurementStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadFixedMeasurementsService @Inject constructor(
    @Authenticated private val apiService: ApiService,
    private val errorHandler: ErrorHandler
) {
    suspend fun upload(
        sessionUUID: String,
        deviceId: String,
        csvMeasurementStream: CSVMeasurementStream,
        csvMeasurements: List<CSVMeasurement>
    ): Result<Unit> = runCatching {
        val params = UploadFixedMeasurementsParams(
            sessionUUID,
            deviceId,
            csvMeasurementStream,
            csvMeasurements
        )
        val gzippedParams = GzippedParams.get(params, UploadFixedMeasurementsParams::class.java)

        val response =
            apiService.uploadFixedMeasurements(UploadFixedMeasurementsBody(gzippedParams))

        if (!response.isSuccessful) {
            throw UnexpectedAPIError()
        }
    }.onFailure { throwable ->
        errorHandler.handle(UnexpectedAPIError(throwable))
    }
}
