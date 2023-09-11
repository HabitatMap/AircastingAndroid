package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor

import android.util.Log
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSessionFileHandlerMobile
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.CSVMeasurement
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.CSVSession
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.lineParameter.CSVLineParameterHandler

class SDCardMobileSessionsProcessor @AssistedInject constructor(
    @Assisted fileHandlerMobile: SDCardSessionFileHandlerMobile,
    mSessionsRepository: SessionsRepository,
    mMeasurementStreamsRepository: MeasurementStreamsRepository,
    mMeasurementsRepository: MeasurementsRepositoryImpl,
    private val settings: Settings,
    @Assisted private val lineParameterHandler: CSVLineParameterHandler,
) : SDCardSessionsProcessor(
    fileHandlerMobile,
    mSessionsRepository,
    mMeasurementStreamsRepository,
    mMeasurementsRepository,
    lineParameterHandler
) {
    private var lastLat: Double? = null
    private var lastLng: Double? = null

    override suspend fun processSession(deviceId: String, csvSession: CSVSession?) {
        csvSession?.uuid ?: return

        val dbSession = mSessionsRepository.getSessionByUUID(csvSession.uuid)
        val session: Session?
        val sessionId: Long


        if (dbSession == null) {
            Log.v(TAG, "Could not find session with uuid: ${csvSession.uuid} in DB")
            session = csvSession.toSession(deviceId, lineParameterHandler.deviceType) ?: return
            sessionId = mSessionsRepository.insert(session)
        } else {
            session = Session(dbSession)
            sessionId = dbSession.id
            val cords = mMeasurementStreamsRepository.getLastKnownLatLng(sessionId)
            lastLat = cords.latitude
            lastLng = cords.longitude
        }

        Log.v(TAG, "Will save measurements: ${session.isDisconnected()}")
        if (session.isDisconnected()) {
            csvSession.streams.forEach { (headerKey, csvMeasurements) ->
                processMeasurements(deviceId, sessionId, headerKey, csvMeasurements)
            }

            finishSession(sessionId, session)
        }
    }

    private suspend fun finishSession(sessionId: Long, session: Session) {
        val lastMeasurementTime = mMeasurementsRepository.lastMeasurementTime(sessionId)
        session.stopRecording(lastMeasurementTime)
        mSessionsRepository.update(session)
        settings.decreaseActiveMobileSessionsCount()
    }

    override suspend fun filterMeasurements(
        sessionId: Long,
        measurementStreamId: Long,
        csvMeasurements: List<CSVMeasurement>
    ): List<CSVMeasurement> {

        if(lastLat != null && lastLng != null) {
            csvMeasurements.forEach {
                if (it.latitude == null && it.longitude == null) {
                    it.latitude = lastLat
                    it.longitude = lastLng
                }
            }
        }
        val lastMeasurementTime =
            mMeasurementsRepository.lastMeasurementTime(sessionId, measurementStreamId)
                ?: return csvMeasurements


        return csvMeasurements.filter { csvMeasurement -> csvMeasurement.time > lastMeasurementTime }
    }
}
