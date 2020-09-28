package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.screens.common.MeasurementsTableContainer
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.Session

abstract class SessionViewMvcImpl<ListenerType>: BaseObservableViewMvc<ListenerType>,
    SessionViewMvc<ListenerType>, BottomSheet.Listener {
    protected val mLayoutInflater: LayoutInflater
    protected val mMeasurementsTableContainer: MeasurementsTableContainer

    private val mDateTextView: TextView
    private val mNameTextView: TextView
    private val mTagsTextView: TextView
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

        this.rootView = inflater.inflate(R.layout.session_card, parent, false)
        mSupportFragmentManager = supportFragmentManager

        mDateTextView = findViewById(R.id.session_date)
        mNameTextView = findViewById(R.id.session_name)
        mTagsTextView = findViewById(R.id.session_tags)

        mMeasurementsTableContainer = MeasurementsTableContainer(context, inflater, this.rootView, false, showMeasurementsTableValues())

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

    override fun bindSession(session: Session) {
        resetCardState()
        bindSessionDetails(session)
        mMeasurementsTableContainer.bindSession(session, mSelectedStream, this::onMeasurementStreamChanged)
    }

    private fun resetCardState() {
        collapseSessionCard()
    }

    protected fun bindSessionDetails(session: Session) {
        mSession = session
        mSelectedStream = session.streamsSortedByDetailedType().firstOrNull()

        mDateTextView.text = session.durationString()
        mNameTextView.text = session.name
        mTagsTextView.text = session.tagsString()
    }

    protected open fun expandSessionCard() {
        mExpandSessionButton.visibility = View.INVISIBLE
        mCollapseSessionButton.visibility = View.VISIBLE
        mExpandedSessionView.visibility = View.VISIBLE

        mMeasurementsTableContainer.makeSelectable()

        onExpandSessionCard()
    }

    protected open fun collapseSessionCard() {
        mCollapseSessionButton.visibility = View.INVISIBLE
        mExpandSessionButton.visibility = View.VISIBLE
        mExpandedSessionView.visibility = View.GONE

        mMeasurementsTableContainer.makeStatic(showMeasurementsTableValues())
    }

    protected fun onMeasurementStreamChanged(measurementStream: MeasurementStream) {
        mSelectedStream = measurementStream
    }

    private fun onMapButtonClicked() {
        mSession?.let {
            for (listener in listeners) {
                (listener as? SessionCardListener)?.onMapButtonClicked(it, mSelectedStream)
            }
        }
    }

    private fun onExpandSessionCard() {
        mSession?.let {
            for (listener in listeners) {
                (listener as? SessionCardListener)?.onExpandSessionCard(it)
            }
        }
    }
}
