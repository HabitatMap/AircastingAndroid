package pl.llp.aircasting.screens.dashboard.charts

import pl.llp.aircasting.models.Session
import java.util.*
import kotlin.time.minutes


class ChartRefreshService {
    private val MINUTE_IN_MILLISECONDS = 60 * 1000
    private val HOUR_IN_MILLISECONDS = 60 * 60 * 1000

    private var mLastRefreshTime: Long? = null
    private var mRefreshFrequency: Int
    private val mSession: Session?

    constructor(
        session: Session?
    ) {
        mSession = session
        mRefreshFrequency = getRefreshFrequency()
    }

    fun setLastRefreshTime() {
        mLastRefreshTime = System.currentTimeMillis()
    }

    fun shouldBeRefreshed(): Boolean {
        return when(mSession?.type) {
            Session.Type.MOBILE -> mLastRefreshTime == null || (timeFromLastRefresh() >= mRefreshFrequency)
            Session.Type.FIXED -> Date().minutes == 0
            else -> mLastRefreshTime == null || (timeFromLastRefresh() >= mRefreshFrequency)
        }
    }

    private fun timeFromLastRefresh(): Long {
        return System.currentTimeMillis() - (mLastRefreshTime ?: 0)
    }

    private fun getRefreshFrequency(): Int {
        return when (mSession?.type) {
            Session.Type.MOBILE -> MINUTE_IN_MILLISECONDS
            Session.Type.FIXED -> HOUR_IN_MILLISECONDS
            else -> MINUTE_IN_MILLISECONDS
        }
    }
}
