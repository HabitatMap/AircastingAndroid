package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync

import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.util.extensions.runOnIOThread
import java.io.File

class SDCardFixedSessionsProcessor(
    mCSVFileFactory: SDCardCSVFileFactory,
    mSDCardCSVIterator: SDCardCSVIterator,
    mSessionsRepository: SessionsRepository,
    mMeasurementStreamsRepository: MeasurementStreamsRepository,
    mMeasurementsRepository: MeasurementsRepositoryImpl
) : SDCardSessionsProcessor(
    mCSVFileFactory,
    mSDCardCSVIterator,
    mSessionsRepository,
    mMeasurementStreamsRepository,
    mMeasurementsRepository
) {
    override val file: File?
        get() = mCSVFileFactory.getFixedDirectory()

    override fun run(deviceId: String, onFinishCallback: ((MutableList<Long>) -> Unit)?) {
        super.run(deviceId, null)
    }

    fun run(deviceId: String) {
        runOnIOThread {
            val file = file ?: return@runOnIOThread

            mSDCardCSVIterator.run(file).forEach { csvSession ->
                processSession(deviceId, csvSession)
            }
        }
    }

    override fun processSession(deviceId: String, csvSession: CSVSession?) {
        csvSession ?: return

        val dbSession = mSessionsRepository.getSessionByUUID(csvSession.uuid)
        val sessionId: Long? = dbSession?.id

        sessionId ?: return

        csvSession.streams.forEach { (headerKey, csvMeasurements) ->
            processMeasurements(deviceId, sessionId, headerKey, csvMeasurements)
        }
    }
}
