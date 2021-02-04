package io.lunarlogic.aircasting.sensor.airbeam3

import android.content.Context
import com.opencsv.CSVReader
import io.lunarlogic.aircasting.sensor.airbeam3.DownloadFromSDCardService.Header
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.MeasurementsFromSDCardParsingError
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
                val value = line[streamHeader.value].toInt() // TODO: handle parse issues

                if (!streams.containsKey(streamHeader.value)) {
                    streams[streamHeader.value] = ArrayList()
                }

                val measurement = CSVMeasurement(value, latitude, longitude, time)
                streams[streamHeader.value]?.add(measurement)
            }
        }
    }

    class CSVMeasurement(val value: Int, val latitude: Double?, val longitude: Double?, time: Date)

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
        csvSession.streams.forEach { (header, measurements) ->
            println("ANIA ${header}: ${measurements.size}")
        }

//        // TODO: arrange it in a better way
//        DatabaseProvider.runQuery {
//            if (mSession == null || mSession?.uuid != uuid) {
//                mDBSession = mSessionsRepository.getSessionWithMeasurementsByUUID(uuid)
//
//                if (mDBSession != null) mSession = Session(mDBSession!!)
//            }
//
//            if (mSession == null) {
//                // TODO: create session
//            } else if (mSession?.isDisconnected() == true) { // TODO: add fixed flow?
//                val sessionId = mDBSession?.session?.id
//
//                sessionId?.let {
//                    streams.each { streamHeader ->
//                        val measurementStream = MeasurementStream.fromHeader(streamHeader)
//                        val measurementStreamId = mMeasurementStreamsRepository.getIdOrInsert(
//                            sessionId,
//                            measurementStream
//                        )
//
//
//                        mMeasurementsRepository.insertAll(measurementStreamId, sessionId, measurements)
//                    }
//                }
//            }
//        }
    }
}
