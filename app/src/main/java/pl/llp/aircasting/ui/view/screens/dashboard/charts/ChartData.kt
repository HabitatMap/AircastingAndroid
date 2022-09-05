package pl.llp.aircasting.ui.view.screens.dashboard.charts

import com.github.mikephil.charting.data.Entry
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session

class ChartData(private var mSession: Session) {
    var entriesStartTime: String? = null
    var entriesEndTime: String? = null

    private val chartDataProvider: SessionChartDataCalculator =
        if (mSession.isExternal)
            ExternalSessionChartDataCalculator(mSession)
        else SessionChartDataCalculator(mSession)

    private var mEntriesPerStream: HashMap<String, List<Entry>>
    private var mChartRefreshService = ChartRefreshService(mSession)

    init {
        entriesStartTime = chartDataProvider.mStartTimeToDisplay
        entriesEndTime = chartDataProvider.mEndTimeToDisplay
        mEntriesPerStream = chartDataProvider.mEntriesPerStream

        mChartRefreshService.setLastRefreshTime()
    }

    fun getEntries(stream: MeasurementStream?): List<Entry>? {
        return mEntriesPerStream[chartDataProvider.streamKey(stream)]
    }

    fun refresh(session: Session) {
        val hourChanged = mSession.endTime?.hours != session.endTime?.hours

        mSession = session
        if (mChartRefreshService.isTimeToRefresh() || hourChanged) {
            chartDataProvider.refresh(session)
            mChartRefreshService.setLastRefreshTime()

            entriesStartTime = chartDataProvider.mStartTimeToDisplay
            entriesEndTime = chartDataProvider.mEndTimeToDisplay
            mEntriesPerStream = chartDataProvider.mEntriesPerStream
        }
    }
}
