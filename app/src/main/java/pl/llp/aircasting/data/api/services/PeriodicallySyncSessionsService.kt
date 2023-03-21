package pl.llp.aircasting.data.api.services

import android.util.Log
import pl.llp.aircasting.data.api.response.SyncResponse
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.util.Settings
import retrofit2.Call

// We need this service to handle deleting sessions in quick succession.
// It periodically checks if sync is needed (it's possible that sync() didn't trigger after deleting sessions when we delete a couple sessions one after another quickly
class PeriodicallySyncSessionsService(
    private val settings: Settings,
    private val sessionsSyncService: SessionsSyncService
    ) {
    private val thread = SyncThread()

    fun start() {
        thread.start()
    }

    fun stop() {
        thread.cancel()
    }

    fun pause() {
        thread.paused = true
    }

    fun resume() {
        thread.paused = false
    }

    inner class SyncThread : Thread() {
        private val POLL_INTERVAL = 60 * 1000L // 1 minute
        var paused = false
        private var call: Call<SyncResponse>? = null


        override fun run() {
            try {
                while (!isInterrupted && settings.thereAreSessionsToRemove()) {
                    Log.d(TAG, "Starting periodic sessions sync")
                    syncSessions()
                    sleep(POLL_INTERVAL)

                    while (paused) {
                        sleep(1000)
                    }
                }
            } catch (e: InterruptedException) {
                return
            }
        }

        fun cancel() {
            interrupt()
            call?.cancel()
        }

        private fun syncSessions() {
            sessionsSyncService.sync()
            settings.setSessionsToRemove(false)
        }
    }
}
