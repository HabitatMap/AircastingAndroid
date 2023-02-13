package pl.llp.aircasting.utilities

import com.google.gson.Gson
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import pl.llp.aircasting.data.api.response.StreamOfGivenSessionResponse
import pl.llp.aircasting.data.api.response.search.SessionsInRegionsResponse
import pl.llp.aircasting.data.local.entity.MeasurementDBObject
import pl.llp.aircasting.data.model.Measurement
import java.io.File
import java.io.FileReader
import java.util.*

object StubData {
    fun measurementsFrom(file: File): MutableList<Measurement> {
        val measurements = LinkedList<Measurement>()

        val reader = CSVReaderBuilder(FileReader(file))
            .withCSVParser(
                CSVParserBuilder()
                    .withSeparator(';')
                    .build()
            )
            .build()
        reader.readNext()

        var line = reader.readNext()
        while (line != null) {
            val value = line[2].toDouble()
            val time = Date(line[3].toLong())
            val latitude = line[4].toDouble()
            val longitude = line[5].toDouble()
            val averagingFrequency = line[6].toInt()

            measurements.add(Measurement(value, time, latitude, longitude, averagingFrequency))

            line = reader.readNext()
        }

        return measurements
    }

    fun measurementsFrom(fileName: String): MutableList<Measurement> =
        measurementsFrom(getFile(fileName))

    fun dbMeasurementsFrom(fileName: String): MutableList<MeasurementDBObject> {
        val file = getFile(fileName)
        val measurements = LinkedList<MeasurementDBObject>()

        val reader = CSVReaderBuilder(FileReader(file))
            .withCSVParser(
                CSVParserBuilder()
                    .withSeparator(';')
                    .build()
            )
            .build()
        reader.readNext()

        var line = reader.readNext()
        while (line != null) {
            val streamId = line[0].toLong()
            val sessionId = line[1].toLong()
            val value = line[2].toDouble()
            val time = Date(line[3].toLong())
            val latitude = line[4].toDouble()
            val longitude = line[5].toDouble()
            val averagingFrequency = line[6].toInt()
            val id = line[7].toLong()

            val measurement = MeasurementDBObject(
                streamId,
                sessionId,
                value,
                time,
                latitude,
                longitude,
                averagingFrequency
            )
            measurement.id = id

            measurements.add(
                measurement
            )

            line = reader.readNext()
        }

        return measurements
    }

    fun getJson(path: String): String {
        val uri = ClassLoader.getSystemResource(path)
        return File(uri.path).readText()
    }

    fun getFile(path: String): File {
        val uri = ClassLoader.getSystemResource(path)
        return File(uri.path)
    }

    fun mockGetSessionsInRegionResponseWithJson(json: String): SessionsInRegionsResponse {
        return Gson().fromJson(json, SessionsInRegionsResponse::class.java)
    }

    fun mockGetStreamOfGivenSessionResponseWithJson(json: String): StreamOfGivenSessionResponse {
        return Gson().fromJson(json, StreamOfGivenSessionResponse::class.java)
    }
}