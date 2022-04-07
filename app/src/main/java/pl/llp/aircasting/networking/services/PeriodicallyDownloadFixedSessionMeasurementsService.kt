package pl.llp.aircasting.networking.services

import android.content.Context
import androidx.work.*
import androidx.work.PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS
import androidx.work.WorkRequest.MIN_BACKOFF_MILLIS
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.networking.responses.SessionWithMeasurementsResponse
import retrofit2.Call
import java.util.concurrent.TimeUnit

class PeriodicallyDownloadFixedSessionMeasurementsService(
    mContext: Context,
    apiService: ApiService,
    errorHandler: ErrorHandler
) {
    companion object {
        const val workerTag = "download_measurements_worker"
        var call: Call<SessionWithMeasurementsResponse>? = null
        val sessionsRepository = SessionsRepository()
    }

    private val downloadMeasurementsService = DownloadMeasurementsService(apiService, errorHandler)
    var mWorkManager = WorkManager.getInstance(mContext)

    fun downloadMeasurements() {
        val dbSessions = sessionsRepository.fixedSessions()
        dbSessions.forEach { dbSession ->
            val session = Session(dbSession)
            downloadMeasurements(dbSession.id, session)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun downloadMeasurements(sessionId: Long, session: Session) {
        GlobalScope.launch(Dispatchers.IO) {
            call = downloadMeasurementsService.enqueueDownloadingMeasurementsForFixed(
                sessionId,
                session
            )
        }
    }

    /*
    * Can be configured in repeatIntervals() / setInitialDelay() / and setBackoffCriteria() for settings attempts.
    * Currently, it should be running after "5 * 60 * 1000L" -> (5 minutes) periodically.
    * Initial delay is set to 1 minute.
    * Attempt: Default is 10 attempts.
    * This also checks if battery is not low.
    */
    fun start() {
        val constraints: Constraints = Constraints.Builder().apply {
            setRequiredNetworkType(NetworkType.CONNECTED)
            setRequiresBatteryNotLow(true)
        }.build()

        val request: PeriodicWorkRequest = PeriodicWorkRequest.Builder(
            DownloadFixedSessionMeasurementsWorker::class.java,
            MIN_PERIODIC_FLEX_MILLIS,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .setBackoffCriteria(BackoffPolicy.LINEAR, MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
            .build()
        mWorkManager.enqueueUniquePeriodicWork(
            workerTag,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun stop() {
        mWorkManager.cancelAllWorkByTag(workerTag)
    }

    fun pause() {
        // TODO
    }

    fun resume() {
        // TODO
    }
}

