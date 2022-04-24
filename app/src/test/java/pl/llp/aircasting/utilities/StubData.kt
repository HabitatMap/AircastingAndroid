package pl.llp.aircasting.utilities

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import pl.llp.aircasting.data.model.Measurement
import java.io.File
import java.io.FileReader
import java.util.*

class StubData {
    companion object {
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
    }
}