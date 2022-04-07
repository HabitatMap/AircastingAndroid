package pl.llp.aircasting.networking.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import pl.llp.aircasting.lib.isWorkEverScheduledBefore
import pl.llp.aircasting.networking.services.PeriodicallyDownloadFixedSessionMeasurementsService.Companion.workerTag

class DownloadFixedSessionMeasurementsWorker(val appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        try {
            while (!isWorkEverScheduledBefore(appContext, workerTag)) {

                println("start: worker is working")
                // downloadmeasurements() method should be called

            }

            Result.success()
        } catch (e: InterruptedException) {
            Result.failure()
        }

        return Result.success()
    }
}