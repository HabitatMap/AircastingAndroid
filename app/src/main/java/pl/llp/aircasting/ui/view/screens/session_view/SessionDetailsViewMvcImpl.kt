package pl.llp.aircasting.ui.view.screens.session_view

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.hlu_slider.view.*
import kotlinx.android.synthetic.main.session_details.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.ui.view.common.BaseObservableViewMvc
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.session_view.hlu.HLUDialog
import pl.llp.aircasting.ui.view.screens.session_view.hlu.HLUDialogListener
import pl.llp.aircasting.ui.view.screens.session_view.hlu.HLUSlider
import pl.llp.aircasting.ui.view.screens.session_view.measurement_table_container.MeasurementsTableContainer
import pl.llp.aircasting.ui.view.screens.session_view.measurement_table_container.SessionDetailsMeasurementsTableContainer
import pl.llp.aircasting.util.DurationStringHelper
import pl.llp.aircasting.util.extensions.gone
import pl.llp.aircasting.util.extensions.startAnimation
import pl.llp.aircasting.util.extensions.stopAnimation

abstract class SessionDetailsViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    supportFragmentManager: FragmentManager?
) : BaseObservableViewMvc<SessionDetailsViewMvc.Listener>(), SessionDetailsViewMvc,
    HLUDialogListener {
    private val mFragmentManager: FragmentManager? = supportFragmentManager
    private var mListener: SessionDetailsViewMvc.Listener? = null

    private val mSessionDateTextView: TextView?
    private val mSessionNameTextView: TextView?
    protected val mSessionMeasurementsDescription: TextView?

    protected var mSessionPresenter: SessionPresenter? = null

    private val mMeasurementsTableContainer: MeasurementsTableContainer
    protected var mStatisticsContainer: StatisticsContainer?
    private val mMoreButton: ImageView?
    private val mMoreInvisibleButton: Button?
    private val mHLUSlider: HLUSlider

    private val mMeasurementsRepository = MeasurementsRepository()
    private var using24HourFormat: Boolean? = true

    init {
        this.rootView = inflater.inflate(layoutId(), parent, false)
        mSessionDateTextView = this.rootView?.session_date
        mSessionNameTextView = this.rootView?.session_name
        mSessionMeasurementsDescription = this.findViewById(R.id.session_measurements_description)
        mMeasurementsTableContainer = SessionDetailsMeasurementsTableContainer(
            context,
            inflater,
            this.rootView,
            selectable = true,
            displayValues = true
        )
        mStatisticsContainer = if (shouldShowStatisticsContainer()) {
            StatisticsContainer(this.rootView, context)
        } else {
            null
        }
        mMoreButton = this.rootView?.more_button
        mMoreInvisibleButton = this.rootView?.more_invisible_button
        mMoreButton?.setOnClickListener { onMoreButtonPressed() }
        mMoreInvisibleButton?.setOnClickListener { onMoreButtonPressed() }

        mHLUSlider = HLUSlider(this.rootView, context, this::onSensorThresholdChanged)
        mSessionMeasurementsDescription.gone()
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

    override fun addMeasurement(measurement: Measurement) {}

    override fun bindSession(sessionPresenter: SessionPresenter?) {
        mListener?.refreshSession()
        mSessionPresenter = sessionPresenter

        if (mSessionPresenter?.selectedStream?.measurements?.isNotEmpty() == true) {
            bindSessionDetails()
            showSlider()
            mMeasurementsTableContainer.bindSession(
                mSessionPresenter,
                this::onMeasurementStreamChanged
            )

            bindStatisticsContainer()
            mHLUSlider.bindSensorThreshold(sessionPresenter?.selectedSensorThreshold(), sessionPresenter?.selectedStream)
            mSessionMeasurementsDescription?.visibility = View.VISIBLE
        }
    }

    protected open fun shouldShowStatisticsContainer(): Boolean {
        return true
    }

    override fun refreshStatisticsContainer() {
        mStatisticsContainer?.refresh(mSessionPresenter)
    }

    private fun showSlider() {
        val visibilityIndex = context.resources.getInteger(R.integer.visible_in_larger_screens)
        /**
         * 0 means visible, see VISIBILITY_FLAGS in View class:
         * private static final int[] VISIBILITY_FLAGS = {VISIBLE, INVISIBLE, GONE};
         * we keep them in integers.xml as indexes (0, 1, 2) instead of values (0, 4, 8)
         * because this is how they are used in XML
         */

        if (visibilityIndex != 0) {
            mMoreButton?.visibility = View.GONE
        } else {
            mMoreButton?.visibility = View.VISIBLE
        }
        mHLUSlider.show()
    }

    override fun centerMap(location: Location) {}

    private fun bindSessionDetails() {
        val session = mSessionPresenter?.session
        session ?: return

        mSessionDateTextView?.text =
            DurationStringHelper().durationString(session.startTime, session.endTime)

        mSessionNameTextView?.text = session.name
        bindSessionMeasurementsDescription()
    }

    protected open fun bindSessionMeasurementsDescription() {
        mSessionMeasurementsDescription?.text = context.getString(R.string.parameters)
    }

    protected open fun bindStatisticsContainer() {
        if (shouldShowStatisticsContainer()) {
            mStatisticsContainer?.bindSession(mSessionPresenter)
        }
    }

    protected open fun onMeasurementStreamChanged(measurementStream: MeasurementStream) {
        mSessionPresenter?.selectedStream = measurementStream
        mStatisticsContainer?.refresh(mSessionPresenter)
        bindSession(mSessionPresenter)
    }

    protected open fun onSensorThresholdChanged(sensorThreshold: SensorThreshold) {
        mMeasurementsTableContainer.refresh(mSessionPresenter)
        mStatisticsContainer?.refresh(mSessionPresenter)

        mListener?.onSensorThresholdChanged(sensorThreshold)
    }

    protected open fun onNoteAdded(note: Note) {
        mSessionPresenter?.session?.notes?.add(note)
    }

    protected open fun onNoteDeleted(note: Note) {
        mSessionPresenter?.session?.notes?.remove(note)
    }

    override fun onSensorThresholdChangedFromDialog(sensorThreshold: SensorThreshold) {
        onSensorThresholdChanged(sensorThreshold)
        mHLUSlider.refresh(sensorThreshold, mSessionPresenter?.selectedStream)
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

    fun showLoader(loader: ImageView?) {
        loader?.startAnimation()
    }

    fun hideLoader(loader: ImageView?) {
        loader?.stopAnimation()
    }
}
