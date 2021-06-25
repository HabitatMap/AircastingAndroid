package pl.llp.aircasting.networking.services

import java.util.*

class LastMeasurementSyncCalculator {
    companion object {
        private val MEASUREMENTS_TIMEFRAME = 24 * 60 * 60 * 1000 // 24 hours in milliseconds

        fun calculate(sessionEndTime: Date?, lastMeasurementTime: Date?): Date {
            // endTime should be saved after session sync in SessionsSyncService
            // but I'm leaving fallback to Date(), just to be sure it will not crash
            val sessionEndTime = sessionEndTime ?: Date()

            lastMeasurementTime?.let { lastMeasurementTime ->
                if (sessionEndTime.time - lastMeasurementTime.time < MEASUREMENTS_TIMEFRAME) {
                    return lastMeasurementTime
                } else {
                    return Date(sessionEndTime.time - MEASUREMENTS_TIMEFRAME)
                }
            }

            return Date(sessionEndTime.time - MEASUREMENTS_TIMEFRAME)
        }
    }
}
