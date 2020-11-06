package io.lunarlogic.aircasting.screens.session_view.map

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
import io.lunarlogic.aircasting.screens.session_view.MeasurementsTableContainer
import io.lunarlogic.aircasting.screens.session_view.SessionViewMvc
import io.lunarlogic.aircasting.screens.session_view.StatisticsContainer
import kotlinx.android.synthetic.main.activity_map.view.*


abstract class MapViewMvcImpl: BaseObservableViewMvc<SessionViewMvc.Listener>, SessionViewMvc, HLUDialogListener {
    private val mFragmentManager: FragmentManager?
    private var mListener: SessionViewMvc.Listener? = null

    private val mSessionDateTextView: TextView?
    private val mSessionNameTextView: TextView?
    private val mSessionTagsTextView: TextView?
    protected val mSessionMeasurementsDescription: TextView?

    private var mSessionPresenter: SessionPresenter? = null

    private val mMeasurementsTableContainer: MeasurementsTableContainer
    private val mMapContainer: MapContainer
    protected var mStatisticsContainer: StatisticsContainer?
    private val mMoreButton: ImageView?
    private val mHLUSlider: HLUSlider

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        supportFragmentManager: FragmentManager?
    ): super() {
        this.mFragmentManager = supportFragmentManager

        this.rootView = inflater.inflate(R.layout.activity_map, parent, false)

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
        mMapContainer = MapContainer(rootView, context, supportFragmentManager)
        mStatisticsContainer = StatisticsContainer(this.rootView, context)
        mMoreButton = this.rootView?.more_button
        mMoreButton?.setOnClickListener {
            onMoreButtonPressed()
        }
        mHLUSlider = HLUSlider(this.rootView, context, this::onSensorThresholdChanged)
    }

    override fun registerListener(listener: SessionViewMvc.Listener) {
        super.registerListener(listener)
        mListener = listener
        mMapContainer.registerListener(listener)
    }

    override fun unregisterListener(listener: SessionViewMvc.Listener) {
        super.unregisterListener(listener)
        mListener = null
        mMapContainer.unregisterListener()
    }

    override fun addMeasurement(measurement: Measurement) {
        mMapContainer.addMeasurement(measurement)
        mStatisticsContainer?.addMeasurement(measurement)
    }

    override fun bindSession(sessionPresenter: SessionPresenter?) {
        mSessionPresenter = sessionPresenter

        bindSessionDetails()
        if (sessionPresenter?.selectedStream != null) showSlider()

        mMapContainer.bindSession(mSessionPresenter)
        mMeasurementsTableContainer.bindSession(mSessionPresenter, this::onMeasurementStreamChanged)
        bindStatisticsContainer()
        mHLUSlider.bindSensorThreshold(sessionPresenter?.selectedSensorThreshold())
    }

    open fun bindStatisticsContainer() {
        mStatisticsContainer?.bindSession(mSessionPresenter)
    }

    fun showSlider() {
        mMoreButton?.visibility = View.VISIBLE
        mHLUSlider.show()
    }

    override fun centerMap(location: Location) {
        mMapContainer.centerMap(location)
    }

    private fun bindSessionDetails() {
        val session = mSessionPresenter?.session
        session ?: return

        mSessionDateTextView?.text = session.durationString()
        mSessionNameTextView?.text = session.name
        mSessionTagsTextView?.text = session.infoString()
        bindSessionMeasurementsDescription()
    }

    open fun bindSessionMeasurementsDescription() {
        mSessionMeasurementsDescription?.text = context.getString(R.string.parameters)
    }

    private fun onMeasurementStreamChanged(measurementStream: MeasurementStream) {
        mSessionPresenter?.selectedStream = measurementStream
        mMapContainer.refresh(mSessionPresenter)
        mStatisticsContainer?.refresh(mSessionPresenter)
        mHLUSlider.refresh(mSessionPresenter?.selectedSensorThreshold())
    }

    private fun onSensorThresholdChanged(sensorThreshold: SensorThreshold) {
        mMeasurementsTableContainer.refresh()
        mMapContainer.refresh(mSessionPresenter)
        mStatisticsContainer?.refresh(mSessionPresenter)

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
