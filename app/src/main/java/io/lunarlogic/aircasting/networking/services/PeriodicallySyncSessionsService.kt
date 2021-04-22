package io.lunarlogic.aircasting.networking.services

import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.responses.SyncResponse
import retrofit2.Call

class PeriodicallySyncSessionsService(
    private val apiService: ApiService,
    private val errorHandler: ErrorHandler,
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

    inner class SyncThread(): Thread() {
        private val POLL_INTERVAL = 60 * 1000L // 1 minute
        var paused = false
        private var call: Call<SyncResponse>? = null


        override fun run() {
            try {
                while (!isInterrupted && settings.getAreThereSessionsToRemove() == true) {
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
