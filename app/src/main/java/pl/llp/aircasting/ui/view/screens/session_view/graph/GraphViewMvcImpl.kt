package pl.llp.aircasting.ui.view.screens.session_view.graph

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.activity_graph.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Note
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvc
import pl.llp.aircasting.ui.view.screens.session_view.SessionDetailsViewMvcImpl
import java.util.*


abstract class GraphViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    protected val supportFragmentManager: FragmentManager
) : SessionDetailsViewMvcImpl(inflater, parent, supportFragmentManager) {
    private var graphContainer: GraphContainer?
    private val mLoader: ImageView?
    private var mSessionActionsButton: ImageView? =
        rootView?.findViewById(R.id.session_actions_button)
    private var mBottomSheet: BottomSheet? = null

    init {
        graphContainer = GraphContainer(
            rootView,
            context,
            defaultZoomSpan(),
            this::onTimeSpanChanged,
            this::measurementsSample,
            notes()
        )
        mSessionActionsButton?.setOnClickListener {
            showBottomSheet(supportFragmentManager)
        }
        mLoader = rootView?.loader_graph
        showLoader(mLoader)
    }

    abstract fun defaultZoomSpan(): Int?

    open fun measurementsSample(): List<Measurement> {
        return mSessionPresenter?.selectedStream?.measurements ?: listOf()
    }

    open fun notes(): List<Note> {
        return mSessionPresenter?.session?.notes ?: listOf()
    }

    override fun layoutId(): Int {
        return R.layout.activity_graph
    }

    override fun registerListener(listener: SessionDetailsViewMvc.Listener) {
        super.registerListener(listener)
        graphContainer?.registerListener(listener)
    }

    override fun unregisterListener(listener: SessionDetailsViewMvc.Listener) {
        super.unregisterListener(listener)
        graphContainer?.unregisterListener()
    }

    override fun bindSession(sessionPresenter: SessionPresenter?) {
        super.bindSession(sessionPresenter)
        graphContainer?.bindSession(mSessionPresenter)
        if (mSessionPresenter?.selectedStream?.measurements?.isNotEmpty() == true) hideLoader(
            mLoader
        )
    }

    override fun onMeasurementStreamChanged(measurementStream: MeasurementStream) {
        super.onMeasurementStreamChanged(measurementStream)
        graphContainer?.refresh(mSessionPresenter)
    }

    override fun onSensorThresholdChanged(sensorThreshold: SensorThreshold) {
        super.onSensorThresholdChanged(sensorThreshold)
        graphContainer?.refresh(mSessionPresenter)
    }

    override fun addNote(note: Note) {
        graphContainer?.refresh(mSessionPresenter)
    }

    override fun deleteNote(note: Note) {
        graphContainer?.refresh(mSessionPresenter)
    }

    private fun onTimeSpanChanged(timeSpan: ClosedRange<Date>) {
        mSessionPresenter?.visibleTimeSpan = timeSpan
        mStatisticsContainer?.refresh(mSessionPresenter)
    }

    override fun onDestroy() {
        graphContainer?.destroy()
        graphContainer = null
    }

    private fun showBottomSheet(supportFragmentManager: FragmentManager) {
        mSessionPresenter?.buildActionsBottomSheet(this, supportFragmentManager)
            ?.show(supportFragmentManager)
    }

    protected fun dismissBottomSheet() {
        mBottomSheet?.dismiss()
    }
}
