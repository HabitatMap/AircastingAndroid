package pl.llp.aircasting.services


class AveragingBackgroundService(val averagingService: AveragingService) {
    private val DEFAULT_INTERVAL = AveragingService.FIRST_THRESHOLD_FREQUENCY * 1000L
    private val thread = AveragingThread()

    fun start() {
        thread.start()
    }

    fun stop() {
        thread.cancel()
    }

    private inner class AveragingThread() : Thread() {

        override fun run() {
            try {
                while (!isInterrupted) {
                    val averagingTime = averageMeasurements()
                    sleep(currentInterval(averagingTime))
                }
            } catch (e: InterruptedException) {
                return
            }
        }

        fun cancel() {
            averageMeasurementsFinal()
            interrupt()
        }

        private fun averageMeasurements(): Long {
            val begin = System.currentTimeMillis()
            averagingService.perform()
            val end = System.currentTimeMillis()

            return end - begin
        }

        private fun averageMeasurementsFinal() {
            averagingService.perform(true)
        }

        private fun currentInterval(averagingTime: Long): Long {
            var interval = DEFAULT_INTERVAL
            averagingService.currentAveragingThreshold()?.windowSize?.let {
                if (it > 1) interval = it * 1000L
            }

            interval -= averagingTime
            if (interval < 0) interval = 0

            return interval
        }
    }
}
