package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.sessionProcessor

import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import pl.llp.aircasting.data.local.entity.MeasurementDBObject
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardSessionFileHandlerFixed
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.CSVMeasurement
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.CSVSession
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.lineParameter.CSVLineParameterHandler

class SDCardFixedSessionsProcessor @AssistedInject constructor(
    @Assisted mSDCardCSVIterator: SDCardSessionFileHandlerFixed,
    mSessionsRepository: SessionsRepository,
    mMeasurementStreamsRepository: MeasurementStreamsRepository,
    mMeasurementsRepository: MeasurementsRepositoryImpl,
    @Assisted lineParameterHandler: CSVLineParameterHandler,
) : SDCardSessionsProcessor(
    mSDCardCSVIterator,
    mSessionsRepository,
    mMeasurementStreamsRepository,
    mMeasurementsRepository,
    lineParameterHandler
) {
    override suspend fun processSession(deviceId: String, csvSession: CSVSession?) {
        csvSession?.uuid ?: return

        val dbSession = mSessionsRepository.getSessionByUUID(csvSession.uuid)
        val sessionId: Long? = dbSession?.id

        sessionId ?: return

        csvSession.streams.forEach { (abLineParameter, csvMeasurements) ->
            processMeasurements(deviceId, sessionId, abLineParameter, csvMeasurements)
        }
    }

    override suspend fun filterMeasurements(
        sessionId: Long,
        measurementStreamId: Long,
        csvMeasurements: List<CSVMeasurement>
    ): List<CSVMeasurement> {
        val measurementsInDB =
            mMeasurementsRepository.getBySessionIdAndStreamId(sessionId, measurementStreamId)

        return csvMeasurements.filter { csvMeasurement ->
            isNotAlreadyInDB(csvMeasurement, measurementsInDB)
        }
    }

    private fun isNotAlreadyInDB(
        csvMeasurement: CSVMeasurement,
        measurementsInDB: List<MeasurementDBObject?>
    ): Boolean {
        val index = measurementsInDB.binarySearch{it?.time?.compareTo(csvMeasurement.time) ?: -1}
        return index < 0
    }
}
