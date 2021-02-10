package io.lunarlogic.aircasting.sensor.airbeam3.sync

import android.content.Context
import com.opencsv.CSVReader
import io.lunarlogic.aircasting.sensor.airbeam3.sync.SDCardCSVFileFactory.Header
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.exceptions.MeasurementsFromSDCardParsingError
import io.lunarlogic.aircasting.lib.DateConverter
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import java.io.FileReader
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SDCardMeasurementsCreator(
    private val mContext: Context,
    private val mErrorHandler: ErrorHandler,
    private val mSessionsRepository: SessionsRepository,
    private val mMeasurementStreamsRepository: MeasurementStreamsRepository,
    private val mMeasurementsRepository: MeasurementsRepository
) {
    private val mCSVFileFactory = SDCardCSVFileFactory(mContext)

    class CSVSession(val uuid: String, val streams: HashMap<Int, ArrayList<CSVMeasurement>> = HashMap()) {
        companion object {
            const val DEFAULT_NAME = "Imported from SD card"
            const val DATE_FORMAT = "MM/dd/yyyy HH:mm:ss"

            fun uuidFrom(line: Array<String>): String? {
                return line[Header.UUID.value]
            }
        }

        fun startTime(): Date? {
            return firstMeasurement()?.time
        }

        fun latitude(): Double? {
            return firstMeasurement()?.latitude
        }

        fun longitude(): Double? {
            return firstMeasurement()?.longitude
        }

        private fun firstMeasurement(): CSVMeasurement? {
            // all streams are saved at the same time, so it does not matter which we take
            val stream = streams.values.firstOrNull()
            return stream?.firstOrNull()
        }

        fun addMeasurements(line: Array<String>) {
            val latitude = line[Header.LATITUDE.value].toDouble() // TODO: handle parse issues
            val longitude = line[Header.LONGITUDE.value].toDouble() // TODO: handle parse issues
            val dateString = "${line[Header.DATE.value]} ${line[Header.TIME.value]}"
            val time = DateConverter.fromString(dateString,
                DATE_FORMAT
            )

            time ?: return

            val supportedStreamHeaders = CSVMeasurementStream.SUPPORTED_STREAMS.keys
            supportedStreamHeaders.forEach { streamHeader ->
                val value = line[streamHeader.value].toDouble() // TODO: handle parse issues

                if (!streams.containsKey(streamHeader.value)) {
                    streams[streamHeader.value] = ArrayList()
                }

                val measurement =
                    CSVMeasurement(
                        value,
                        latitude,
                        longitude,
                        time
                    )
                streams[streamHeader.value]?.add(measurement)
            }
        }
    }

    class CSVMeasurementStream(
        val sensorName: String,
        val measurementType: String,
        val measurementShortType: String,
        val unitName: String,
        val unitSymbol: String,
        val thresholdVeryLow: Int,
        val thresholdLow: Int,
        val thresholdMedium: Int,
        val thresholdHigh: Int,
        val thresholdVeryHigh: Int
    ) {
        companion object {
            private const val DEVICE_NAME = "AirBeam3"
            private const val PM_MEASUREMENT_TYPE = "Particulate Matter"
            private const val PM_MEASUREMENT_SHORT_TYPE = "PM"
            private const val PM_UNIT_NAME = "micrograms per cubic meter"
            private const val PM_UNIT_SYMBOL = "µg/m³"

            val SUPPORTED_STREAMS = hashMapOf(
                Header.F to CSVMeasurementStream(
                    "$DEVICE_NAME-F",
                    "Temperature",
                    "F",
                    "degrees Fahrenheit",
                    "F",
                    15,
                    45,
                    75,
                    100,
                    135
                ),
                Header.RH to CSVMeasurementStream(
                    "$DEVICE_NAME-RH",
                    "Humidity",
                    "RH",
                    "percent",
                    "%",
                    0,
                    25,
                    50,
                    75,
                    100
                ),
                Header.PM1 to CSVMeasurementStream(
                    "$DEVICE_NAME-PM1",
                    PM_MEASUREMENT_TYPE,
                    PM_MEASUREMENT_SHORT_TYPE,
                    PM_UNIT_NAME,
                    PM_UNIT_SYMBOL,
                    0,
                    12,
                    35,
                    55,
                    150
                ),
                Header.PM2_5 to CSVMeasurementStream(
                    "$DEVICE_NAME-PM2.5",
                    PM_MEASUREMENT_TYPE,
                    PM_MEASUREMENT_SHORT_TYPE,
                    PM_UNIT_NAME,
                    PM_UNIT_SYMBOL,
                    0,
                    12,
                    35,
                    55,
                    150
                ),
                Header.PM10 to CSVMeasurementStream(
                    "$DEVICE_NAME-PM10",
                    PM_MEASUREMENT_TYPE,
                    PM_MEASUREMENT_SHORT_TYPE,
                    PM_UNIT_NAME,
                    PM_UNIT_SYMBOL,
                    0,
                    20,
                    50,
                    100,
                    200
                )
            )

            fun fromHeader(streamHeader: Header): CSVMeasurementStream? {
                return SUPPORTED_STREAMS[streamHeader]
            }
        }

        fun sensorPackageName(deviceId: String): String {
            return "$DEVICE_NAME:${deviceId}"
        }
    }

    class CSVMeasurement(val value: Double, val latitude: Double?, val longitude: Double?, val time: Date)

    fun run(deviceId: String) {
        val file = mCSVFileFactory.get()
        val reader = CSVReader(FileReader(file))

        try {
            processFile(deviceId, reader)
        } catch (e: IOException) {
            mErrorHandler.handle(MeasurementsFromSDCardParsingError(e))
        }
    }

    private fun processFile(deviceId: String, reader: CSVReader) {
        var previousSessionUUID: String? = null
        var currentSession: CSVSession? = null

        do {
            val line = reader.readNext()

            if (line == null) {
                if (currentSession != null) {
                    processSession(deviceId, currentSession)
                }
                break
            }

            val currentSessionUUID =
                CSVSession.uuidFrom(
                    line
                )

            if (currentSessionUUID != previousSessionUUID) {
                if (currentSession != null) {
                    processSession(deviceId, currentSession)
                }

                currentSession =
                    CSVSession(
                        currentSessionUUID!!
                    )
                previousSessionUUID = currentSessionUUID
            }

            currentSession?.addMeasurements(line)
        } while(line != null)
    }

    private fun processSession(deviceId: String, csvSession: CSVSession) {
        DatabaseProvider.runQuery {
            val dbSession = mSessionsRepository.getSessionByUUID(csvSession.uuid)
            val session: Session
            val sessionId: Long

            if (dbSession == null) {
                // TODO: use proper session type while implementing fixed flow
                session = Session(
                    csvSession.uuid,
                    deviceId,
                    DeviceItem.Type.AIRBEAM3,
                    Session.Type.MOBILE,
                    CSVSession.DEFAULT_NAME,
                    ArrayList(),
                    Session.Status.DISCONNECTED,
                    csvSession.startTime()!! // TODO: handle in better way
                )

                val latitude = csvSession.latitude()
                val longitude = csvSession.longitude()
                if (latitude != null && longitude != null) {
                    val location = Session.Location(latitude, longitude)
                    session.location = location

                    if (location == Session.Location.INDOOR_FAKE_LOCATION) {
                        session.locationless = true
                    }
                }

                sessionId = mSessionsRepository.insert(session)
            } else {
                session = Session(dbSession)
                sessionId = dbSession.id
            }

            if (session.isDisconnected()) { // TODO: add fixed flow?
                csvSession.streams.forEach { (headerKey, csvMeasurements) ->
                    processMeasurements(deviceId, sessionId, session, headerKey, csvMeasurements)
                }

                // TODO: temp - arrange it in a better way..
                session.stopRecording()
                mSessionsRepository.update(session)
            }
        }
    }

    private fun processMeasurements(deviceId: String, sessionId: Long, session: Session, headerKey: Int, csvMeasurements: List<CSVMeasurement>) {
        val streamHeader = Header.fromInt(headerKey)
        val csvMeasurementStream = CSVMeasurementStream.fromHeader(
            streamHeader
        ) ?: return

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
            csvMeasurementStream.thresholdVeryHigh
        )
        val measurementStreamId = mMeasurementStreamsRepository.getIdOrInsert(
            sessionId,
            measurementStream
        )

        val lastMeasurementTime = mMeasurementsRepository.lastMeasurementTime(sessionId, measurementStreamId)

        val filteredCSVMeasurements = if (lastMeasurementTime != null) {
            csvMeasurements.filter { csvMeasurement -> csvMeasurement.time > lastMeasurementTime }
        } else {
            csvMeasurements
        }

        val measurements = filteredCSVMeasurements.map { csvMeasurement ->
            Measurement(csvMeasurement.value, csvMeasurement.time, csvMeasurement.latitude, csvMeasurement.longitude)
        }
        mMeasurementsRepository.insertAll(measurementStreamId, sessionId, measurements)
    }
}
