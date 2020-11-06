package io.lunarlogic.aircasting.screens.session_view

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.SensorThreshold
import io.lunarlogic.aircasting.screens.common.*
import io.lunarlogic.aircasting.screens.session_view.hlu.HLUDialog
import io.lunarlogic.aircasting.screens.session_view.hlu.HLUDialogListener
import io.lunarlogic.aircasting.screens.session_view.hlu.HLUSlider
import kotlinx.android.synthetic.main.activity_map.view.*


abstract class SessionDetailsViewMvcImpl: BaseObservableViewMvc<SessionDetailsViewMvc.Listener>, SessionDetailsViewMvc, HLUDialogListener {
    private val mFragmentManager: FragmentManager?
    private var mListener: SessionDetailsViewMvc.Listener? = null

    private val mSessionDateTextView: TextView?
    private val mSessionNameTextView: TextView?
    private val mSessionTagsTextView: TextView?
    protected val mSessionMeasurementsDescription: TextView?

    protected var mSessionPresenter: SessionPresenter? = null

    private val mMeasurementsTableContainer: MeasurementsTableContainer
    protected var mStatisticsContainer: StatisticsContainer?
    private val mMoreButton: ImageView?
    private val mHLUSlider: HLUSlider

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager?
    ): super() {
        this.mFragmentManager = supportFragmentManager

        this.rootView = inflater.inflate(layoutId(), parent, false)

        mSessionDateTextView = this.rootView?.session_date
        mSessionNameTextView = this.rootView?.session_name
        mSessionTagsTextView = this.rootView?.session_info
        mSessionMeasurementsDescription = this.rootView?.session_measurements_description

        mMeasurementsTableContainer = MeasurementsTableContainer(
            context,
            inflater,
            this.rootView,
            true,
            true
        )

        mStatisticsContainer = StatisticsContainer(this.rootView, context)
        mMoreButton = this.rootView?.more_button
        mMoreButton?.setOnClickListener {
            onMoreButtonPressed()
        }
        mHLUSlider = HLUSlider(this.rootView, context, this::onSensorThresholdChanged)
    }

    abstract fun layoutId(): Int

    override fun registerListener(listener: SessionDetailsViewMvc.Listener) {
        super.registerListener(listener)
        mListener = listener
    }

    override fun unregisterListener(listener: SessionDetailsViewMvc.Listener) {
        super.unregisterListener(listener)
        mListener = null
    }

    override fun addMeasurement(measurement: Measurement) {
        mStatisticsContainer?.addMeasurement(measurement)
    }

    override fun bindSession(sessionPresenter: SessionPresenter?) {
        mSessionPresenter = sessionPresenter

        bindSessionDetails()
        if (sessionPresenter?.selectedStream != null) showSlider()

        mMeasurementsTableContainer.bindSession(mSessionPresenter, this::onMeasurementStreamChanged)
        bindStatisticsContainer()
        mHLUSlider.bindSensorThreshold(sessionPresenter?.selectedSensorThreshold())
    }

    fun showSlider() {
        mMoreButton?.visibility = View.VISIBLE
        mHLUSlider.show()
    }

    override fun centerMap(location: Location) {}

    private fun bindSessionDetails() {
        val session = mSessionPresenter?.session
        session ?: return

        mSessionDateTextView?.text = session.durationString()
        mSessionNameTextView?.text = session.name
        mSessionTagsTextView?.text = session.infoString()
        bindSessionMeasurementsDescription()
    }

    protected open fun bindSessionMeasurementsDescription() {
        mSessionMeasurementsDescription?.text = context.getString(R.string.parameters)
    }

    protected open fun bindStatisticsContainer() {
        mStatisticsContainer?.bindSession(mSessionPresenter)
    }

    protected open fun onMeasurementStreamChanged(measurementStream: MeasurementStream) {
        mSessionPresenter?.selectedStream = measurementStream
        bindStatisticsContainer()
        mHLUSlider.refresh(mSessionPresenter?.selectedSensorThreshold())
    }

    protected open fun onSensorThresholdChanged(sensorThreshold: SensorThreshold) {
        mMeasurementsTableContainer.refresh()
        bindStatisticsContainer()

        mListener?.onSensorThresholdChanged(sensorThreshold)
    }

    override fun onSensorThresholdChangedFromDialog(sensorThreshold: SensorThreshold) {
        onSensorThresholdChanged(sensorThreshold)
        mHLUSlider.refresh(sensorThreshold)
    }

    override fun onValidationFailed() {
        mListener?.onHLUDialogValidationFailed()
    }

    private fun onMoreButtonPressed() {
        mFragmentManager?.let {
            val sensorThreshold = mSessionPresenter?.selectedSensorThreshold()
            val measurementStream = mSessionPresenter?.selectedStream
            HLUDialog(sensorThreshold, measurementStream, mFragmentManager, this).show()
        }
    }
}
