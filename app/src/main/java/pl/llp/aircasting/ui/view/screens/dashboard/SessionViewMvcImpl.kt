package pl.llp.aircasting.ui.view.screens.dashboard

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.expanded_session_view.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BaseObservableViewMvc
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.charts.Chart
import pl.llp.aircasting.ui.view.screens.session_view.measurement_table_container.MeasurementsTableContainer
import pl.llp.aircasting.ui.view.screens.session_view.measurement_table_container.SessionCardMeasurementsTableContainer
import pl.llp.aircasting.util.*


abstract class SessionViewMvcImpl<ListenerType>(
    inflater: LayoutInflater,
    parent: ViewGroup,
    supportFragmentManager: FragmentManager
) : BaseObservableViewMvc<ListenerType>(),
    SessionViewMvc<ListenerType> {
    protected val mLayoutInflater: LayoutInflater = inflater
    protected val mMeasurementsTableContainer: MeasurementsTableContainer

    protected val mSessionCardLayout: ViewGroup

    private val mDateTextView: TextView
    private val mNameTextView: TextView
    private val mInfoTextView: TextView
    protected val mActionsButton: ImageView
    private val mSupportFragmentManager: FragmentManager = supportFragmentManager
    private var mBottomSheet: BottomSheet? = null

    protected var mExpandedSessionView: View
    protected var mExpandSessionButton: ImageView
    protected var mCollapseSessionButton: ImageView
    protected var mReorderSessionButton: ImageView
    private val mChart: Chart
    private val mChartView: ConstraintLayout?
    protected val mMeasurementsDescription: TextView?

    protected var mFollowButton: Button
    protected var mUnfollowButton: Button

    private var mMapButton: Button
    private var mGraphButton: Button
    private var mLoader: ImageView?

    protected var mSessionPresenter: SessionPresenter? = null
    private var expandCardCallback: (() -> Unit?)? = null
    private var onExpandSessionCardClickedCallback: (() -> Unit?)? = null
    private var using24HourFormat: Boolean? = true

    init {
        this.rootView = inflater.inflate(R.layout.session_card, parent, false)
        mSessionCardLayout = findViewById(R.id.session_card_layout)
        mDateTextView = findViewById(R.id.session_date)
        mNameTextView = findViewById(R.id.session_name)
        mInfoTextView = findViewById(R.id.session_info)
        mMeasurementsDescription = findViewById(R.id.session_measurements_description)
        expandCardCallback = { expandSessionCard() }
        onExpandSessionCardClickedCallback = { onExpandSessionCardClicked() }
        mMeasurementsTableContainer = SessionCardMeasurementsTableContainer(
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
            onExpandSessionCardClicked()
            expandSessionCard()
        }
        mCollapseSessionButton = findViewById(R.id.collapse_session_button)
        mCollapseSessionButton.setOnClickListener {
            onCollapseSessionCardClicked()
            collapseSessionCard()
        }
        mReorderSessionButton = findViewById(R.id.reorder_session_button)
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
        mLoader = rootView?.findViewById(R.id.loader)
    }

    protected abstract fun showMeasurementsTableValues(): Boolean
    protected abstract fun showExpandedMeasurementsTableValues(): Boolean
    protected abstract fun buildBottomSheet(sessionPresenter: SessionPresenter?): BottomSheet?

    protected open fun showChart() = true

    private fun actionsButtonClicked() {
        mBottomSheet = buildBottomSheet(mSessionPresenter)
        mBottomSheet?.show(mSupportFragmentManager)
    }

    protected fun dismissBottomSheet() {
        mBottomSheet?.dismiss()
    }

    override fun bindSession(sessionPresenter: SessionPresenter) {
        // TODO: check what is going on with binding measurements table because it is bind 6 times every second
        bindLoader(sessionPresenter)
        bindSelectedStream(sessionPresenter)
        bindExpanded(sessionPresenter)
        bindSessionDetails()
        bindMeasurementsDescription(sessionPresenter)
        bindMeasurementsTable()
        bindChartData()
        bindFollowButtons(sessionPresenter)
        bindMapButton(sessionPresenter)
        bindMeasurementsSelectable(
            mMeasurementsTableContainer,
            onExpandSessionCardClickedCallback,
            expandCardCallback
        )
    }

    protected open fun bindMeasurementsSelectable(
        mMeasurementsTableContainer: MeasurementsTableContainer,
        onExpandSessionCardClickedCallback: (() -> Unit?)?,
        expandCardCallback: (() -> Unit?)?
    ) {
        mMeasurementsTableContainer.makeSelectable(showMeasurementsTableValues())
        mMeasurementsTableContainer.bindExpandCardCallbacks(
            expandCardCallback,
            onExpandSessionCardClickedCallback
        )
    }

    private fun bindLoader(sessionPresenter: SessionPresenter) {
        if (sessionPresenter.loading) showLoader() else hideLoader()
    }

    protected open fun bindExpanded(sessionPresenter: SessionPresenter) {
        if (sessionPresenter.expanded) expandSessionCard() else collapseSessionCard()
    }

    private fun bindSelectedStream(sessionPresenter: SessionPresenter) {
        mSessionPresenter = sessionPresenter
        if (mSessionPresenter != null && sessionPresenter.selectedStream == null) {
            mSessionPresenter?.setDefaultStream()
        }
    }

    private fun bindSessionDetails() {
        val session = mSessionPresenter?.session

        mDateTextView.text = DurationStringHelper().durationString(
            session?.startTime!!,
            session.endTime
        )
        mNameTextView.text = session.name
        mInfoTextView.text = session.infoString()
    }

    private fun bindMeasurementsDescription(sessionPresenter: SessionPresenter) {
        val sessionStatus = sessionPresenter.session?.status
        val sessionIsDisconnected = Session.Status.DISCONNECTED
        val isFixedSession = sessionPresenter.isFixed()

        when {
            sessionStatus == sessionIsDisconnected && !isFixedSession -> mMeasurementsDescription?.gone()
            sessionPresenter.expanded -> bindExpandedMeasurementsDescription()
            else -> bindCollapsedMeasurementsDescription()
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
        mFollowButton.gone()
        mUnfollowButton.gone()
    }

    protected open fun bindMapButton(sessionPresenter: SessionPresenter) {
        if (sessionPresenter.shouldHideMap) {
            mMapButton.gone()
        } else {
            mMapButton.visible()
        }
    }

    protected open fun expandSessionCard() {
        setExpandCollapseButton()
        mExpandedSessionView.visible()
        if (showExpandedMeasurementsTableValues()) {
            mMeasurementsTableContainer.makeSelectable()
        }

        if (showChart()) {
            mChartView?.visibility = View.VISIBLE
        }
        bindExpandedMeasurementsDescription()

        adjustSessionCardPadding()

        expandButtonsHitAreas(
            listOf(mGraphButton, mMapButton, mUnfollowButton, mFollowButton),
            mExpandedSessionView
        )
    }

    protected open fun collapseSessionCard() {
        setExpandCollapseButton()
        mExpandedSessionView.gone()

        bindCollapsedMeasurementsDescription()
        mMeasurementsTableContainer.makeCollapsed(showMeasurementsTableValues())

        adjustSessionCardPadding()
    }

    protected open fun setExpandCollapseButton() {
        if (mSessionPresenter?.expanded == true) {
            mExpandSessionButton.inVisible()
            mCollapseSessionButton.visible()
            mReorderSessionButton.inVisible()
        } else {
            mExpandSessionButton.visible()
            mCollapseSessionButton.inVisible()
            mReorderSessionButton.inVisible()
        }
    }

    protected open fun bindExpandedMeasurementsDescription() {
        mMeasurementsDescription?.text = context.getString(R.string.parameters)
    }

    protected open fun bindCollapsedMeasurementsDescription() {
        mMeasurementsDescription?.text = context.getString(R.string.parameters)
    }

    override fun showLoader() {
        mLoader?.startAnimation()
        mFollowButton.isEnabled = false
    }

    override fun hideLoader() {
        mLoader?.stopAnimation()
        mFollowButton.isEnabled = true
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
            bindFollowButtons(mSessionPresenter!!)

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
        mSessionPresenter?.session?.let {
            for (listener in listeners) {
                (listener as? SessionCardListener)?.onCollapseSessionCard(it)
            }
        }
    }

    private fun getExpandedTouchDelegate(child: View): TouchDelegate {
        val paddingX = 10
        val paddingY = 40
        val rect = Rect()
        child.getHitRect(rect)
        rect.left -= paddingX
        rect.top -= paddingY
        rect.right += paddingX
        rect.bottom += paddingY

        return TouchDelegate(rect, child)
    }

    private fun expandButtonsHitAreas(buttons: List<View>, parentView: View) {
        val touchDelegateComposite = TouchDelegateComposite(parentView)

        buttons.forEach { button ->
            touchDelegateComposite.addDelegate(getExpandedTouchDelegate(button))
        }

        parentView.post {
            parentView.touchDelegate = touchDelegateComposite
        }
    }

    /**
     * In order to really increase extended card buttons' touch area
     * we need to make expanded_session_view container bigger
     * by increasing bottom padding. We need to remove session card padding
     * when the card is expanded and add it back when it is collapsed
     */
    private fun adjustSessionCardPadding() {
        if (mSessionPresenter?.expanded == true) {
            mSessionCardLayout.setPadding(
                mSessionCardLayout.paddingLeft,
                mSessionCardLayout.paddingTop,
                mSessionCardLayout.paddingRight,
                0
            )
        } else {
            mSessionCardLayout.setPadding(
                mSessionCardLayout.paddingLeft,
                mSessionCardLayout.paddingTop,
                mSessionCardLayout.paddingRight,
                mSessionCardLayout.paddingRight
            )
        }
    }
}
