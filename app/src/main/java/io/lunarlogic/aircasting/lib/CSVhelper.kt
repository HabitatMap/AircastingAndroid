package io.lunarlogic.aircasting.lib

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.common.io.Closer
import com.opencsv.CSVWriter
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.Session
import java.io.*
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Created by Maria Turnau on 22/06/2021.
 */
/**
 * AirCasting - Share your Air!
 * Copyright (C) 2011-2012 HabitatMap, Inc.
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 *
 * You can contact the authors by email at <info></info>@habitatmap.org>
 */


class CSVHelper {
    private val ZIP_EXTENSION = ".zip"
    private val CSV_EXTENSION = ".csv"
    val closer = Closer.create()

    @Throws(IOException::class)
    fun prepareCSV(context: Context, session: Session): Uri {
        return try {
            val storage = context.filesDir
            val dir = File(storage, "aircasting_sessions")
            dir.mkdirs()
            val file = File(dir, fileName(session.name) + ZIP_EXTENSION)
            val outputStream: OutputStream = FileOutputStream(file)
            closer.register(outputStream)
            val zippedOutputStream =
                ZipOutputStream(outputStream)
            zippedOutputStream.putNextEntry(ZipEntry(fileName(session.name) + CSV_EXTENSION))
            val writer: Writer = OutputStreamWriter(zippedOutputStream)
            val csvWriter = CSVWriter(writer, ',',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)
            write(session).toWriter(csvWriter)
            csvWriter.flush()
            csvWriter.close()

            //            Uri uri = Uri.fromFile(file);
            val uri: Uri =
                FileProvider.getUriForFile(context, "io.lunarlogic.aircasting.fileprovider", file)

            uri
        } finally {
            closer.close()
        }
    }

    fun fileName(title: String): String {
        val result = StringBuilder()
        if (title.isNotEmpty()) {
            try {
                val matcher =
                    Pattern.compile("([_\\-a-zA-Z0-9])*")
                        .matcher(title.toLowerCase())
                while (matcher.find()) {
                    result.append(matcher.group())
                }
            } catch (ignore: IllegalStateException) {
            }
        }
        return if (result.length > MINIMUM_SESSION_NAME_LENGTH) result.toString() else SESSION_FALLBACK_FILE
    }

    private fun write(session: Session): SessionWriter {
        return SessionWriter(session)
    }

    companion object {
        const val TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

        // Gmail app hack - it requires all file attachments to begin with /mnt/sdcard
        const val SESSION_ZIP_FILE = "aircasting_session_archive"
        const val SESSION_FALLBACK_FILE = "session_data"
        private const val MINIMUM_SESSION_NAME_LENGTH = 2
    }
}

internal class SessionWriter(session: Session) {
    val TIMESTAMP_FORMAT =
        SimpleDateFormat(CSVHelper.TIMESTAMP_FORMAT)
    var session: Session

    @Throws(IOException::class)
    fun toWriter(writer: CSVWriter) {
        val streams: Iterable<MeasurementStream> =
            session.activeStreams
        for (stream in streams) {
            writeSensorHeader(writer)
            writeSensor(stream, writer)
            writeMeasurementHeader(writer)
            for (measurement in stream.measurements) {
                writeMeasurement(writer, measurement)
            }
        }
    }

    @Throws(IOException::class)
    private fun writeMeasurementHeader(writer: CSVWriter) {
        writer.writeNext(arrayOf("Timestamp",
        "geo:lat",
        "geo:long",
        "Value"))
    }

    @Throws(IOException::class)
    private fun writeMeasurement(writer: CSVWriter, measurement: Measurement) {
        writer.writeNext(
            arrayOf(TIMESTAMP_FORMAT.format(measurement.time),
            measurement.latitude.toString(),
            measurement.longitude.toString(),
            measurement.value.toString())
        )
    }

    @Throws(IOException::class)
    private fun writeSensor(stream: MeasurementStream, writer: CSVWriter) {
        writer.writeNext(
            arrayOf((stream.sensorName),
        stream.sensorPackageName,
        stream.measurementType,
        stream.unitName))
    }

    @Throws(IOException::class)
    private fun writeSensorHeader(writer: CSVWriter) {
        writer.writeNext(arrayOf("sensor:model",
        "sensor:package",
        "sensor:capability",
        "sensor:units"))
    }

    init {
        this.session = session
    }
}
