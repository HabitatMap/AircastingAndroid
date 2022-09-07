package pl.llp.aircasting.util

import java.util.*

class DateUtil {
    companion object {
        private fun getStartTime(): Long {
            val startTime = Date()
            return startTime.time
        }

        fun dateFromFloat(mFloatDate: Float): Date {
            return Date(mFloatDate.toLong() + getStartTime())
        }

        /**
         * We need to subtract startTime because
         * otherwise we lose precision while converting Long to Float
         * and Float is needed for the MPAndroidChart library
         **/
        fun convertDateToFloat(date: Date): Float {
            return (date.time - getStartTime()).toFloat()
        }
    }
}