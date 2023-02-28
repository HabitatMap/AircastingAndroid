package pl.llp.aircasting.util.helpers.services

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class AveragingBackgroundService(
    val averagingService: AveragingService,
    private val coroutineContext: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val DEFAULT_INTERVAL = AveragingService.FIRST_THRESHOLD_FREQUENCY * 1000L
    private lateinit var job: Job

    fun start() {
        job = coroutineContext.launch {
            while (true) {
                val averagingTime = averageMeasurements()
                delay(currentInterval(averagingTime))
            }
        }
    }

    fun stop() {
        coroutineContext.launch {
            job.cancelAndJoin()
            averageMeasurementsFinal()
        }
    }

    private suspend fun averageMeasurements(): Long {
        val begin = System.currentTimeMillis()
        val end = coroutineScope {
            async {
                launch {
                    averagingService.perform()
                }.join()
                System.currentTimeMillis()
            }
        }.await()
        return end - begin
    }

    private suspend fun averageMeasurementsFinal() {
        averagingService.perform(true)
    }

    private suspend fun currentInterval(averagingTime: Long): Long {
        var interval = DEFAULT_INTERVAL
        averagingService.currentAveragingThresholdSuspend().windowSize.let {
            if (it > 1)
                interval = it * 1000L
        }

        interval -= averagingTime

        if (interval < 0)
            interval = 0

        return interval
    }
}
