package io.lunarlogic.aircasting.sensor.airbeam3

import android.content.Context
import com.opencsv.CSVReader
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.sensor.airbeam3.DownloadFromSDCardService.Header
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.MeasurementsFromSDCardParsingError
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MeasurementsFromSDCardCreator(
    private val mContext: Context,
    private val mErrorHandler: ErrorHandler,
    private val mSessionsRepository: SessionsRepository,
    private val mMeasurementStreamsRepository: MeasurementStreamsRepository,
    private val mMeasurementsRepository: MeasurementsRepository
) {

    class CSVSession(val uuid: String, val streams: HashMap<Int, ArrayList<CSVMeasurement>> = HashMap()) {
        val SUPPORTED_STREAMS = arrayOf(Header.F, Header.HUMIDITY, Header.PM1, Header.PM2, Header.PM10)

        companion object {
            fun uuidFrom(line: List<String>): String? {
                return line[Header.UUID.value]
            }
        }

        fun addMeasurements(line: List<String>) {
            val latitude = line[Header.LATITUDE.value].toDouble() // TODO: handle parse issues
            val longitude = line[Header.LONGITUDE.value].toDouble() // TODO: handle parse issues
            val time = Date("${line[Header.DATE.value]} ${line[Header.TIME.value]}") // TODO: replace with parsing

            SUPPORTED_STREAMS.forEach { streamHeader ->
                val value = line[streamHeader.value].toDouble() // TODO: handle parse issues

                if (!streams.containsKey(streamHeader.value)) {
                    streams[streamHeader.value] = ArrayList()
                }

                val measurement = CSVMeasurement(value, latitude, longitude, time)
                streams[streamHeader.value]?.add(measurement)
            }
        }
    }

    class CSVMeasurement(val value: Double, val latitude: Double?, val longitude: Double?, val time: Date)

    fun run() {
        // TODO: change naming and extract it somewhere
        val dir = mContext.getExternalFilesDir("sync")
        val file = File(dir, "sync.txt")
        val reader = CSVReader(FileReader(file))

        try {
            processFile(reader)
        } catch (e: IOException) {
            mErrorHandler.handle(MeasurementsFromSDCardParsingError(e))
        }
    }

    private fun processFile(reader: CSVReader) {
        var previousSessionUUID: String? = null
        var currentSession: CSVSession? = null

        do {
            val line = reader.readNext()?.toList() // TODO: remove?

            if (line == null) {
                if (currentSession != null) {
                    processSession(currentSession)
                }
                break
            }

            val currentSessionUUID = CSVSession.uuidFrom(line)

            if (currentSessionUUID != previousSessionUUID) {
                if (currentSession != null) {
                    processSession(currentSession)
                }

                currentSession = CSVSession(currentSessionUUID!!)
                previousSessionUUID = currentSessionUUID
            }

            currentSession?.addMeasurements(line)
        } while(line != null)
    }

    private fun processSession(csvSession: CSVSession) {
        println("ANIA " + csvSession.uuid)

        // TODO: arrange it in a better way
        DatabaseProvider.runQuery {
            val dbSessionWithMeasurements = mSessionsRepository.getSessionWithMeasurementsByUUID(csvSession.uuid)
            val dbSession = dbSessionWithMeasurements?.session
            val session: Session
            val sessionId: Long

            if (dbSession == null) {
                // TODO: use proper device name
                // TODO: use proper session type
                // TODO: use proper start date - from first measurement
                session = Session(
                    csvSession.uuid,
                    "deviceName",
                    DeviceItem.Type.AIRBEAM3,
                    Session.Type.MOBILE,
                    "imported from SD card",
                    ArrayList(),
                    Session.Status.DISCONNECTED,
                    Date()
                )
                sessionId = mSessionsRepository.insert(session)
            } else {
                session = Session(dbSessionWithMeasurements)
                sessionId = dbSession.id
            }

            if (session.isDisconnected()) { // TODO: add fixed flow?
                csvSession.streams.forEach { (headerKey, csvMeasurements) ->
                    val streamHeader = Header.fromInt(headerKey)
                    val measurementStream = MeasurementStream.fromHeader(streamHeader)
                    val measurementStreamId = mMeasurementStreamsRepository.getIdOrInsert(
                        sessionId,
                        measurementStream
                    )

                    // TODO: discard the one we already have
                    val measurements = csvMeasurements.map { csvMeasurement ->
                        Measurement(csvMeasurement.value, csvMeasurement.time, csvMeasurement.latitude, csvMeasurement.longitude)
                    }

                    mMeasurementsRepository.insertAll(measurementStreamId, sessionId, measurements)
                }
            }
        }
    }
}
