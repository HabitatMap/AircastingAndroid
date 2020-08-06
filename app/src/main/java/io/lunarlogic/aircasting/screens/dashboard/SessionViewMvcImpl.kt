package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentManager
import com.baoyz.actionsheet.ActionSheet
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.screens.common.BottomSheet
import io.lunarlogic.aircasting.sensor.Session

abstract class SessionViewMvcImpl<ListenerType>: BaseObservableViewMvc<ListenerType>,
    SessionViewMvc<ListenerType>, ActionSheet.ActionSheetListener, BottomSheet.Listener {
    protected val mLayoutInflater: LayoutInflater

    private val mDateTextView: TextView
    private val mNameTextView: TextView
    private val mTagsTextView: TextView
    private val mMeasurementsTable: TableLayout
    private val mMeasurementHeaders: TableRow
    private val mActionsButton: ImageView
    private val mContext: Context
    private val mSupportFragmentManager: FragmentManager
    private var mBottomSheet :BottomSheet? = null

    private var mSession: Session? = null

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup,
        context: Context,
        supportFragmentManager: FragmentManager
    ) {
        mLayoutInflater = inflater

        this.rootView = inflater.inflate(layoutId(), parent, false)
        mContext = context
        mSupportFragmentManager = supportFragmentManager

        mDateTextView = findViewById(R.id.session_date)
        mNameTextView = findViewById(R.id.session_name)
        mTagsTextView = findViewById(R.id.session_tags)
        mMeasurementsTable = findViewById(R.id.measurements_table)
        mMeasurementHeaders = findViewById(R.id.measurement_headers)
        mActionsButton = findViewById(R.id.session_actions_button)

        mActionsButton.setOnClickListener {
            actionsButtonClicked()
        }
    }

    protected abstract fun layoutId(): Int

    protected fun actionsButtonClicked() {
        mBottomSheet = BottomSheet(this)
        mBottomSheet?.show(mSupportFragmentManager)
    }

    override fun cancelPressed() {
        mBottomSheet?.dismiss()
    }

    override fun bindSession(session: Session) {
        bindSessionDetails(session)
        resetMeasurementsView()
        bindMeasurements(session)
        stretchTableLayout(session)
    }

    protected fun bindSessionDetails(session: Session) {
        mSession = session
        mDateTextView.text = session.durationString()
        mNameTextView.text = session.name
        mTagsTextView.text = session.tags.joinToString(", ")
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
}
