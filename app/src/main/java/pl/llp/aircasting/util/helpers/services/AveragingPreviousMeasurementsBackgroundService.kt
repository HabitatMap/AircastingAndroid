package pl.llp.aircasting.util.helpers.services


class AveragingPreviousMeasurementsBackgroundService(val averagingService: AveragingService) {
    private val DEFAULT_INTERVAL = 60 * 1000L // we run it every minute but perform averaging only when needed
    private val thread = AveragingPreviousMeasurementsThread()

    fun start() {
        thread.start()
    }

    fun stop() {
        thread.cancel()
    }

    private inner class AveragingPreviousMeasurementsThread : Thread() {

        override fun run() {
            try {
                while (!isInterrupted) {
                    averageMeasurements()
                    sleep(DEFAULT_INTERVAL)
                }
            } catch (e: InterruptedException) {
                return
            }
        }

        fun cancel() {
            interrupt()
        }

        private fun averageMeasurements() {
            averagingService.averagePreviousMeasurementsWithNewFrequency()
        }
    }
}
