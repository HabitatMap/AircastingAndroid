package pl.llp.aircasting.data.api.services

import pl.llp.aircasting.data.api.response.SyncResponse
import pl.llp.aircasting.util.helpers.services.AveragingService
import retrofit2.Call

/**
 * Created by Maria Turnau on 28/07/2021.
 */
class AverageAndSyncSDCardSessionsService(
    private val sessionsSyncService: SessionsSyncService,
    private val sessionsIds: MutableList<Long>
) {
    private val thread = SyncThread()

    fun start() {
        thread.start()
    }

    fun stop() {
        thread.cancel()
    }

    inner class SyncThread : Thread() {
        private var call: Call<SyncResponse>? = null


        override fun run() {
            try {
                averageMeasurements()
                syncSessions()

            } catch (e: InterruptedException) {
                return
            }
        }

        fun cancel() {
            interrupt()
            call?.cancel()
        }

        private fun averageMeasurements() {
            sessionsIds.forEach { sessionId ->
                val averagingService = AveragingService.get(sessionId)
                averagingService?.averagePreviousMeasurementsWithNewFrequency()
                averagingService?.perform(true)
            }
        }
        private fun syncSessions() {
            sessionsSyncService.sync()
        }
    }
}
