package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.screens.map.MapActivity
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session

abstract class SessionViewMvcImpl<ListenerType>: BaseObservableViewMvc<ListenerType>,
    SessionViewMvc<ListenerType>, BottomSheet.Listener {
    protected val mLayoutInflater: LayoutInflater

    private val mDateTextView: TextView
    private val mNameTextView: TextView
    private val mTagsTextView: TextView
    private val mMeasurementsTable: TableLayout
    private val mMeasurementHeaders: TableRow
    private val mActionsButton: ImageView
    private val mSupportFragmentManager: FragmentManager
    protected var mBottomSheet: BottomSheet? = null

    private var mExpandedSessionView: View
    protected var mExpandSessionButton: ImageView
    protected var mCollapseSessionButton: ImageView
    private var mMapButton: Button

    protected var mSession: Session? = null
    protected var mSelectedStream: MeasurementStream? = null

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup,
        supportFragmentManager: FragmentManager
    ) {
        mLayoutInflater = inflater

        this.rootView = inflater.inflate(layoutId(), parent, false)
        mSupportFragmentManager = supportFragmentManager

        mDateTextView = findViewById(R.id.session_date)
        mNameTextView = findViewById(R.id.session_name)
        mTagsTextView = findViewById(R.id.session_tags)
        mMeasurementsTable = findViewById(R.id.measurements_table)
        mMeasurementHeaders = findViewById(R.id.measurement_headers)
        mActionsButton = findViewById(R.id.session_actions_button)

        mExpandedSessionView = findViewById(R.id.expanded_session_view)
        mExpandSessionButton = findViewById(R.id.expand_session_button)
        mExpandSessionButton.setOnClickListener {
            expandSessionCard()
        }
        mCollapseSessionButton = findViewById(R.id.collapse_session_button)
        mCollapseSessionButton.setOnClickListener {
            collapseSessionCard()
        }
        mMapButton = findViewById(R.id.map_button)
        mMapButton.setOnClickListener {
            onMapButtonClicked()
        }

        mActionsButton.setOnClickListener {
            actionsButtonClicked()
        }
    }

    protected abstract fun layoutId(): Int
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

    override fun bindSession(session: Session) {
        bindSessionDetails(session)
        resetMeasurementsView()
        bindMeasurements(session)
        stretchTableLayout(session)
    }

    protected fun bindSessionDetails(session: Session) {
        mSession = session
        mSelectedStream = session.streams.firstOrNull()

        mDateTextView.text = session.durationString()
        mNameTextView.text = session.name
        mTagsTextView.text = session.tagsString()
    }

    open protected fun resetMeasurementsView() {
        mMeasurementsTable.isStretchAllColumns = false
        mMeasurementHeaders.removeAllViews()
    }

    open protected fun bindMeasurements(session: Session) {
        session.streams.sortedBy { it.detailedType }.forEach { stream ->
            bindStream(stream.detailedType)
        }
    }

    protected fun stretchTableLayout(session: Session) {
        if (session.streams.size > 1) {
            mMeasurementsTable.isStretchAllColumns = true
        }
    }

    protected fun bindStream(detailedType: String?) {
        val headerView = mLayoutInflater.inflate(R.layout.measurement_header, null, false)

        val headerTextView = headerView.findViewById<TextView>(R.id.measurement_header)
        headerTextView.text = detailedType

        mMeasurementHeaders.addView(headerView)
    }

    private fun expandSessionCard() {
        mExpandSessionButton.visibility = View.INVISIBLE
        mCollapseSessionButton.visibility = View.VISIBLE
        mExpandedSessionView.visibility = View.VISIBLE
    }

    private fun collapseSessionCard() {
        mCollapseSessionButton.visibility = View.INVISIBLE
        mExpandSessionButton.visibility = View.VISIBLE
        mExpandedSessionView.visibility = View.GONE
    }

    abstract protected fun onMapButtonClicked()
}
