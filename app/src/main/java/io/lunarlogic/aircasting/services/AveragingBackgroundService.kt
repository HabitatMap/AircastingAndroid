package io.lunarlogic.aircasting.services


class AveragingBackgroundService(val averagingService: AveragingService) {
    private val DEFAULT_INTERVAL = 5 * 1000L
    private val thread = AveragingThread()

    fun start() {
        println("MARYSIA: start averaging background service")
        thread.start()
    }

    fun stop() {
        println("MARYSIA: stop averaging background service")
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
            interrupt()
        }

        private fun averageMeasurements(): Long {
            val begin = System.currentTimeMillis()
            averagingService.perform()
            val end = System.currentTimeMillis()
//            if (averagingService.mShouldAveragePreviousMeasurements) {
//                averagingService.setShouldAveragePreviousMeasurements(false)
//            }

            return end - begin
        }

        private fun currentInterval(averagingTime: Long): Long {
            println("MARYSIA: current averaging threshold: ${averagingService.currentAveragingThreshold()?.windowSize}")
            var interval = DEFAULT_INTERVAL
            averagingService.currentAveragingThreshold()?.windowSize?.let {
                if (it > 1) interval = it * 1000L
            }

            println("MARYSIA: interval ${interval - averagingTime}")
            return interval - averagingTime
        }
    }
}
