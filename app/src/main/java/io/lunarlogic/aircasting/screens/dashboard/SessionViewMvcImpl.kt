package io.lunarlogic.aircasting.screens.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.AnimatedLoader
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.screens.session_view.MeasurementsTableContainer
import io.lunarlogic.aircasting.screens.dashboard.charts.Chart
import io.lunarlogic.aircasting.models.MeasurementStream
import kotlinx.android.synthetic.main.session_card.view.*

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
    protected val mChart: Chart
    protected val mChartView: ConstraintLayout?
    protected val mMeasurementsDescription: TextView?

    protected var mFollowButton: Button
    protected var mUnfollowButton: Button
    private var mMapButton: Button
    private var mGraphButton: Button
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
        mMeasurementsDescription = findViewById(R.id.session_measurements_description)

        mMeasurementsTableContainer = MeasurementsTableContainer(
            context,
            inflater,
            this.rootView,
            false,
            showMeasurementsTableValues()
        )

        mChart = Chart(
            context,
            this.rootView
        )

        mChartView = rootView?.chart_container

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
        mFollowButton = findViewById(R.id.follow_button)
        mFollowButton.setOnClickListener {
            onFollowButtonClicked()
        }

        mUnfollowButton = findViewById(R.id.unfollow_button)
        mUnfollowButton.setOnClickListener {
            onUnfollowButtonClicked()
        }

        mMapButton = findViewById(R.id.map_button)
        mMapButton.setOnClickListener {
            onMapButtonClicked()
        }

        mGraphButton = findViewById(R.id.graph_button)
        mGraphButton.setOnClickListener {
            onGraphButtonClicked()
        }

        mActionsButton.setOnClickListener {
            actionsButtonClicked()
        }

        mLoader = rootView?.findViewById<ImageView>(R.id.loader)
    }

    protected abstract fun showMeasurementsTableValues(): Boolean
    protected abstract fun showExpandedMeasurementsTableValues(): Boolean
    protected abstract fun buildBottomSheet(): BottomSheet?

    protected open fun showChart() = true

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
        bindLoader(sessionPresenter)
        bindExpanded(sessionPresenter)
        bindSelectedStream(sessionPresenter)
        bindSessionDetails()
        bindMeasurementsDescription(sessionPresenter)
        bindMeasurementsTable()
        bindChartData()
        bindFollowButtons(sessionPresenter)
    }

    private fun bindLoader(sessionPresenter: SessionPresenter) {
        if (sessionPresenter.loading) {
            showLoader()
        } else {
            hideLoader()
        }
    }

    private fun bindExpanded(sessionPresenter: SessionPresenter) {
        if (sessionPresenter.expanded) {
            expandSessionCard()
        } else {
            collapseSessionCard()
        }
    }

    private fun bindSelectedStream(sessionPresenter: SessionPresenter) {
        mSessionPresenter = sessionPresenter
        if (mSessionPresenter != null && sessionPresenter.selectedStream == null) {
            mSessionPresenter!!.setDefaultStream()
        }
    }

    protected fun bindSessionDetails() {
        val session = mSessionPresenter?.session

        mDateTextView.text = session?.durationString()
        mNameTextView.text = session?.name
        mInfoTextView.text = session?.infoString()
    }

    private fun bindMeasurementsDescription(sessionPresenter: SessionPresenter) {
        if (sessionPresenter.expanded) {
            bindExpandedMeasurementsDesctription()
        } else {
            bindCollapsedMeasurementsDesctription()
        }
    }

    protected open fun bindMeasurementsTable() {
        mMeasurementsTableContainer.bindSession(mSessionPresenter, this::onMeasurementStreamChanged)
    }

    private fun bindChartData() {
        if (!showChart()) return

        mChart.bindChart(mSessionPresenter)
    }

    protected open fun bindFollowButtons(sessionPresenter: SessionPresenter) {
        mFollowButton.visibility = View.GONE
        mUnfollowButton.visibility = View.GONE
    }

    protected open fun expandSessionCard() {
        mExpandSessionButton.visibility = View.INVISIBLE
        mCollapseSessionButton.visibility = View.VISIBLE
        mExpandedSessionView.visibility = View.VISIBLE
        if(showExpandedMeasurementsTableValues()){
            mMeasurementsTableContainer.makeSelectable()
        }

        if (showChart()) {
            mChartView?.visibility = View.VISIBLE
        }
        bindExpandedMeasurementsDesctription()
    }

    protected open fun collapseSessionCard() {
        mCollapseSessionButton.visibility = View.INVISIBLE
        mExpandSessionButton.visibility = View.VISIBLE
        mExpandedSessionView.visibility = View.GONE
        mChartView?.visibility = View.GONE

        mMeasurementsTableContainer.makeStatic(showMeasurementsTableValues())
        bindCollapsedMeasurementsDesctription()
    }

    protected open fun bindExpandedMeasurementsDesctription() {
        mMeasurementsDescription?.text = context.getString(R.string.parameters)
    }

    protected open fun bindCollapsedMeasurementsDesctription() {
        mMeasurementsDescription?.text = context.getString(R.string.parameters)
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
        bindChartData()
    }

    private fun onFollowButtonClicked() {
        mSessionPresenter?.session?.let { session ->
            session.follow()
            bindFollowButtons(mSessionPresenter!!)

            for (listener in listeners) {
                (listener as? SessionCardListener)?.onFollowButtonClicked(session)
            }
        }
    }

    private fun onUnfollowButtonClicked() {
        mSessionPresenter?.session?.let { session ->
            session.unfollow()
            for (listener in listeners) {
                (listener as? SessionCardListener)?.onUnfollowButtonClicked(session)
            }
        }
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

    private fun onGraphButtonClicked() {
        mSessionPresenter?.session?.let {
            for (listener in listeners) {
                (listener as? SessionCardListener)?.onGraphButtonClicked(
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
