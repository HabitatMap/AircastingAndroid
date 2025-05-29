package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor

import android.util.Log
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session.Location.Companion.DEFAULT_LOCATION
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.helpers.location.LocationHelper
import pl.llp.aircasting.util.helpers.location.toLatLng
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSessionFileHandlerMobile
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.CSVMeasurement
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.CSVSession
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.lineParameter.CSVLineParameterHandler

class SDCardMobileSessionsProcessor @AssistedInject constructor(
    @Assisted fileHandlerMobile: SDCardSessionFileHandlerMobile,
    mSessionsRepository: SessionsRepository,
    mMeasurementStreamsRepository: MeasurementStreamsRepository,
    mMeasurementsRepository: MeasurementsRepositoryImpl,
    @Assisted private val lineParameterHandler: CSVLineParameterHandler,
) : SDCardSessionsProcessor(
    fileHandlerMobile,
    mSessionsRepository,
    mMeasurementStreamsRepository,
    mMeasurementsRepository,
    lineParameterHandler
) {
    override suspend fun processSession(deviceId: String, csvSession: CSVSession?) {
        csvSession?.uuid ?: return

        val sessionId: Long
        val session: SessionDBObject
        val existingSession = mSessionsRepository.getSessionByUUID(csvSession.uuid)

        if (existingSession == null) {
            Log.v(TAG, "Could not find session with uuid: ${csvSession.uuid} in DB")
            session = csvSession.toSession(deviceId, lineParameterHandler.deviceType) ?: return
            sessionId = mSessionsRepository.insert(session)
        } else {
            session = existingSession
            sessionId = session.id
        }

        Log.v(TAG, "Will save measurements: ${session.isDisconnected}")
        if (session.isDisconnected) {
            csvSession.streams.forEach { (headerKey, csvMeasurements) ->
                processMeasurements(deviceId, sessionId, headerKey, csvMeasurements)
            }
        }
    }

    override suspend fun filterMeasurements(
        sessionId: Long,
        measurementStreamId: Long,
        csvMeasurements: List<CSVMeasurement>
    ): List<CSVMeasurement> {
        if (lineParameterHandler.deviceType == DeviceItem.Type.AIRBEAMMINI) {
            assignLastKnownLocationToCSVMeasurements(sessionId, csvMeasurements)
        }
        val lastMeasurementTime =
            mMeasurementsRepository.lastMeasurementTime(sessionId, measurementStreamId)
                ?: return csvMeasurements


        return csvMeasurements.filter { csvMeasurement -> csvMeasurement.time > lastMeasurementTime }
    }

    private suspend fun assignLastKnownLocationToCSVMeasurements(
        sessionId: Long,
        csvMeasurements: List<CSVMeasurement>
    ) {
        val lastCords = mMeasurementStreamsRepository.getLastKnownLatLng(sessionId)
            ?: LocationHelper.lastLocation()?.toLatLng()
            ?: DEFAULT_LOCATION.toLatLng()

        csvMeasurements.forEach {
            it.latitude = lastCords.latitude
            it.longitude = lastCords.longitude
        }
    }
}
