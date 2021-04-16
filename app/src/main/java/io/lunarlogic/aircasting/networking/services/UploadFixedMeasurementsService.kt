package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.UnexpectedAPIError
import io.lunarlogic.aircasting.networking.GzippedParams
import io.lunarlogic.aircasting.networking.params.UploadFixedMeasurementsBody
import io.lunarlogic.aircasting.networking.params.UploadFixedMeasurementsParams
import io.lunarlogic.aircasting.sensor.airbeam3.sync.CSVMeasurement
import io.lunarlogic.aircasting.sensor.airbeam3.sync.CSVMeasurementStream
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
