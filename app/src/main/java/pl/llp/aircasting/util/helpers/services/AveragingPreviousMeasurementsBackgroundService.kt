package pl.llp.aircasting.util.helpers.services

import kotlinx.coroutines.*


class AveragingPreviousMeasurementsBackgroundService(
    val averagingService: AveragingService,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val DEFAULT_INTERVAL =
        60 * 1000L // we run it every minute but perform averaging only when needed
    private lateinit var job: Job

    fun start() {
//        job = coroutineScope.launch {
//            while (true) {
//                averageMeasurements()
//                delay(DEFAULT_INTERVAL)
//            }
//        }
    }

    fun stop() {
//        thread.cancel()
    }

    private fun averageMeasurements() {
//        averagingService.averagePreviousMeasurementsWithNewFrequency()
    }
}
