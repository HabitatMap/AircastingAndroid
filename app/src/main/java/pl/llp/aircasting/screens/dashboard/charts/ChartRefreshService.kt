package pl.llp.aircasting.screens.dashboard.charts

import pl.llp.aircasting.models.Session
import pl.llp.aircasting.networking.Constants


class ChartRefreshService(session: Session?) {
    private var mLastRefreshTime: Long? = null
    private var mRefreshFrequency: Int
    private val mSession: Session? = session

    init {
        mRefreshFrequency = getRefreshFrequency()
    }

    fun setLastRefreshTime() {
        mLastRefreshTime = System.currentTimeMillis()
    }

    fun isTimeToRefresh(): Boolean {
        return mLastRefreshTime == null || (timeFromLastRefresh() >= mRefreshFrequency)
    }

    private fun timeFromLastRefresh(): Long {
        return System.currentTimeMillis() - (mLastRefreshTime ?: 0)
    }

    private fun getRefreshFrequency(): Int {
        return when (mSession?.type) {
            Session.Type.MOBILE -> Constants.MILLIS_IN_MINUTE
            Session.Type.FIXED -> Constants.MILLIS_IN_HOUR
            else -> Constants.MILLIS_IN_MINUTE
        }
    }
}
