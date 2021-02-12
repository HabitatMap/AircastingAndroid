package io.lunarlogic.aircasting.sensor.airbeam3.sync

import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.networking.services.UploadFixedMeasurementsService

class SDCardUploadFixedMeasurementsService(
    private val mSDCardCSVFileFactory: SDCardCSVFileFactory,
    private val mSDCardCSVIterator: SDCardCSVIterator,
    private val mUploadFixedMeasurementsService: UploadFixedMeasurementsService
) {
    fun run() {
        DatabaseProvider.runQuery {
            val file = mSDCardCSVFileFactory.getFixed()
            val deviceId = "246f28c47698" // TODO: move it to the file name

            mSDCardCSVIterator.run(file).forEach { csvSession ->
                csvSession?.streams?.forEach { (streamHeaderValue, csvMeasurements) ->
                    val streamHeader = SDCardCSVFileFactory.Header.fromInt(streamHeaderValue)
                    val csvMeasurementStream = CSVMeasurementStream.fromHeader(
                        streamHeader
                    )

                    if (csvMeasurementStream != null) {
                        val measurements = csvMeasurements.map { csvMeasurement ->
                            Measurement(csvMeasurement.value, csvMeasurement.time, csvMeasurement.latitude, csvMeasurement.longitude)
                        }

                        val measurementStream = MeasurementStream(
                            csvMeasurementStream.sensorPackageName(deviceId),
                            csvMeasurementStream.sensorName,
                            csvMeasurementStream.measurementType,
                            csvMeasurementStream.measurementShortType,
                            csvMeasurementStream.unitName,
                            csvMeasurementStream.unitSymbol,
                            csvMeasurementStream.thresholdVeryLow,
                            csvMeasurementStream.thresholdLow,
                            csvMeasurementStream.thresholdMedium,
                            csvMeasurementStream.thresholdHigh,
                            csvMeasurementStream.thresholdVeryHigh,
                            false,
                            measurements
                        )

                        // TODO: change it to accept CSVStreamMeasurement?
                        // TODO: check if any measurements are present before sending?
                        println("ANIA uploading ${csvMeasurementStream.sensorName}...")
                        mUploadFixedMeasurementsService.upload(
                            csvSession.uuid,
                            measurementStream,
                            { println("ANIA SUCCESS!") },
                            { println("ANIA ERROR :(") })
                    }
                }
            }
        }
    }
}
