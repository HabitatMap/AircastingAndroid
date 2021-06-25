package pl.llp.aircasting.networking.services

import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.exceptions.UnexpectedAPIError
import pl.llp.aircasting.networking.GzippedParams
import pl.llp.aircasting.networking.params.UploadFixedMeasurementsBody
import pl.llp.aircasting.networking.params.UploadFixedMeasurementsParams
import pl.llp.aircasting.sensor.airbeam3.sync.CSVMeasurement
import pl.llp.aircasting.sensor.airbeam3.sync.CSVMeasurementStream
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UploadFixedMeasurementsService(private val apiService: ApiService, private val errorHandler: ErrorHandler) {
    fun upload(
        sessionUUID: String,
        deviceId: String,
        csvMeasurementStream: CSVMeasurementStream,
        csvMeasurements: List<CSVMeasurement>,
        successCallback: (() -> Unit),
        errorCallback: (() -> Unit)
    ) {
        val params = UploadFixedMeasurementsParams(sessionUUID, deviceId, csvMeasurementStream, csvMeasurements)
        val gzippedParams = GzippedParams.get(params, UploadFixedMeasurementsParams::class.java)

        val call = apiService.uploadFixedMeasurements(UploadFixedMeasurementsBody(gzippedParams))

        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    successCallback.invoke()
                } else {
                    errorHandler.handle(UnexpectedAPIError())
                    errorCallback.invoke()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                errorHandler.handle(UnexpectedAPIError(t))
                errorCallback.invoke()
            }
        })
    }

}
