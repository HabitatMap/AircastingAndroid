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
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.new_session.select_device.DeviceItem
import java.io.FileReader
import java.io.IOException
import kotlin.collections.ArrayList

class SDCardMeasurementsCreator(
    mContext: Context,
    private val mErrorHandler: ErrorHandler,
    private val mSessionsRepository: SessionsRepository,
    private val mMeasurementStreamsRepository: MeasurementStreamsRepository,
    private val mMeasurementsRepository: MeasurementsRepository
) {
    // TODO: inject it too
    private val mCSVFileFactory = SDCardCSVFileFactory(mContext)

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
//            val deviceId = "246f28c47698" // TODO: move it to the file name

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
                // TODO: create stream.measurements instead of this hash?
                csvSession.streams.forEach { (headerKey, csvMeasurements) ->
                    processMeasurements(deviceId, sessionId, headerKey, csvMeasurements)
                }

                // TODO: temp - arrange it in a better way..
                session.stopRecording()
                mSessionsRepository.update(session)
            }
        }
    }

    private fun processMeasurements(deviceId: String, sessionId: Long, streamHeaderValue: Int, csvMeasurements: List<CSVMeasurement>) {
        val streamHeader = Header.fromInt(streamHeaderValue)
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
