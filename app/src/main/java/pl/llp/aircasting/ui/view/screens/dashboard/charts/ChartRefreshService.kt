package pl.llp.aircasting.ui.view.screens.dashboard.charts

import pl.llp.aircasting.data.model.Session


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

    fun isTimeToRefresh(): Boolean {
        return mLastRefreshTime == null || (timeFromLastRefresh() >= mRefreshFrequency)
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