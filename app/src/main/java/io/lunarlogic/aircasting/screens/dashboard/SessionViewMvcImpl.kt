package io.lunarlogic.aircasting.screens.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.AnimatedLoader
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.screens.common.MeasurementsTableContainer
import io.lunarlogic.aircasting.sensor.MeasurementStream

abstract class SessionViewMvcImpl<ListenerType>: BaseObservableViewMvc<ListenerType>,
    SessionViewMvc<ListenerType>, BottomSheet.Listener {
    protected val mLayoutInflater: LayoutInflater
    protected val mMeasurementsTableContainer: MeasurementsTableContainer

    private val mDateTextView: TextView
    private val mNameTextView: TextView
    private val mInfoTextView: TextView
    private val mActionsButton: ImageView
    private val mSupportFragmentManager: FragmentManager
    protected var mBottomSheet: BottomSheet? = null

    private var mExpandedSessionView: View
    protected var mExpandSessionButton: ImageView
    protected var mCollapseSessionButton: ImageView
    protected var mChart: LineChart
    private var mMapButton: Button
    private var mLoader: ImageView?

    protected var mSessionPresenter: SessionPresenter? = null

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup,
        supportFragmentManager: FragmentManager
    ) {
        mLayoutInflater = inflater

        this.rootView = inflater.inflate(R.layout.session_card, parent, false)
        mSupportFragmentManager = supportFragmentManager

        mDateTextView = findViewById(R.id.session_date)
        mNameTextView = findViewById(R.id.session_name)
        mInfoTextView = findViewById(R.id.session_info)

        mMeasurementsTableContainer = MeasurementsTableContainer(
            context,
            inflater,
            this.rootView,
            false,
            showMeasurementsTableValues()
        )

        // CHARTS
        mChart = findViewById<View>(R.id.chart) as LineChart
        val rightYAxis = mChart.axisRight
        rightYAxis.gridColor = ContextCompat.getColor(context, R.color.aircasting_grey_50)
        rightYAxis.setDrawLabels(false)
        val leftYAxis = mChart.axisLeft
        leftYAxis.setDrawLabels(false)
        val xAxis = mChart.xAxis
        xAxis.setDrawLabels(false)
        xAxis.gridColor = Color.TRANSPARENT

        mChart.setDrawBorders(false)
        val entries: List<Entry> = listOf(Entry(0F,1F), Entry(1F,2F), Entry(2F,4F), Entry(3F,2F), Entry(4F,5F), Entry(5F,2F), Entry(6F,4F), Entry(7F,2F), Entry(8F,1F) )
        val dataSet: LineDataSet = LineDataSet(entries, "Label")
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        val lineData: LineData = LineData(dataSet)
        mChart.data = lineData
        mChart.invalidate()
        // CHARTS

        mActionsButton = findViewById(R.id.session_actions_button)

        mExpandedSessionView = findViewById(R.id.expanded_session_view)
        mExpandSessionButton = findViewById(R.id.expand_session_button)
        mExpandSessionButton.setOnClickListener {
            expandSessionCard()
            onExpandSessionCardClicked()
        }
        mCollapseSessionButton = findViewById(R.id.collapse_session_button)
        mCollapseSessionButton.setOnClickListener {
            collapseSessionCard()
            onCollapseSessionCardClicked()
        }
        mMapButton = findViewById(R.id.map_button)
        mMapButton.setOnClickListener {
            onMapButtonClicked()
        }

        mActionsButton.setOnClickListener {
            actionsButtonClicked()
        }

        mLoader = rootView?.findViewById<ImageView>(R.id.loader)
    }

    protected abstract fun showMeasurementsTableValues(): Boolean
    protected abstract fun buildBottomSheet(): BottomSheet?

    private fun actionsButtonClicked() {
        mBottomSheet = buildBottomSheet()
        mBottomSheet?.show(mSupportFragmentManager)
    }

    protected fun dismissBottomSheet() {
        mBottomSheet?.dismiss()
    }

    override fun cancelPressed() {
        dismissBottomSheet()
    }

    override fun bindSession(sessionPresenter: SessionPresenter) {
        if (sessionPresenter.loading) {
            showLoader()
        } else {
            hideLoader()
        }
        if (sessionPresenter.expanded) {
            expandSessionCard()
        } else {
            collapseSessionCard()
        }

        mSessionPresenter = sessionPresenter
        if (mSessionPresenter != null && sessionPresenter.selectedStream == null) {
            mSessionPresenter!!.setDefaultStream()
        }

        bindSessionDetails()
        bindMeasurementsTable()
    }

    protected fun bindSessionDetails() {
        val session = mSessionPresenter?.session

        mDateTextView.text = session?.durationString()
        mNameTextView.text = session?.name
        mInfoTextView.text = "${session?.displayedType?.capitalize()}: ${session?.sensorPackageNamesString()}"
    }

    protected open fun bindMeasurementsTable() {
        mMeasurementsTableContainer.bindSession(mSessionPresenter, this::onMeasurementStreamChanged)
    }

    protected open fun expandSessionCard() {
        mExpandSessionButton.visibility = View.INVISIBLE
        mCollapseSessionButton.visibility = View.VISIBLE
        mExpandedSessionView.visibility = View.VISIBLE
        mChart.visibility = View.VISIBLE

        mMeasurementsTableContainer.makeSelectable()
    }

    protected open fun collapseSessionCard() {
        mCollapseSessionButton.visibility = View.INVISIBLE
        mExpandSessionButton.visibility = View.VISIBLE
        mExpandedSessionView.visibility = View.GONE

        mMeasurementsTableContainer.makeStatic(showMeasurementsTableValues())
    }

    override fun showLoader() {
        AnimatedLoader(mLoader).start()
        mLoader?.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        mLoader?.visibility = View.GONE
    }

    protected fun onMeasurementStreamChanged(measurementStream: MeasurementStream) {
        mSessionPresenter?.selectedStream = measurementStream
    }

    private fun onMapButtonClicked() {
        mSessionPresenter?.session?.let {
            for (listener in listeners) {
                (listener as? SessionCardListener)?.onMapButtonClicked(
                    it,
                    mSessionPresenter?.selectedStream
                )
            }
        }
    }

    private fun onExpandSessionCardClicked() {
        mSessionPresenter?.expanded = true

        mSessionPresenter?.session?.let {
            for (listener in listeners) {
                (listener as? SessionCardListener)?.onExpandSessionCard(it)
            }
        }
    }

    private fun onCollapseSessionCardClicked() {
        mSessionPresenter?.expanded = false
    }
}
