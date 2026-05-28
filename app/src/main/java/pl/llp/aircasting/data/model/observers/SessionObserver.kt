package pl.llp.aircasting.data.model.observers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.SensorName
import pl.llp.aircasting.util.SelectedStreams
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel

abstract class SessionObserver<Type>(
    private val mLifecycleOwner: LifecycleOwner,
    protected val mSessionsViewModel: SessionsViewModel,
    private val mSessionPresenter: SessionPresenter,
    private val onSessionChangedCallback: () -> Unit

) {
    private var mObserver: Observer<Type?> = Observer { sessionDBObject ->
        sessionDBObject?.let {
            val session = buildSession(sessionDBObject)
            if (session.hasChangedFrom(mSessionPresenter.session)) {
                onSessionChanged(session)
            }
        }
    }

    private var mSessionLiveData: LiveData<Type?>? = null

    fun observe() {
        mSessionLiveData = sessionLiveData()
        mSessionLiveData?.observe(mLifecycleOwner, mObserver)
    }

    abstract fun sessionLiveData(): LiveData<Type?>?
    abstract fun buildSession(dbSession: Type): Session

    private fun onSessionChanged(session: Session) {
        mSessionPresenter.session = session

        mLifecycleOwner.lifecycleScope.launch {
            var selectedSensorName = mSessionPresenter.initialSensorName
            if (mSessionPresenter.selectedStream != null) {
                selectedSensorName = mSessionPresenter.selectedStream!!.sensorName
            }

            var measurementStream =
                session.streams.firstOrNull { it.sensorName == selectedSensorName }
            if (measurementStream == null) {
                val sortedByDetailedType = session.streamsSortedByDetailedType()
                val savedStreamDetailedType = SelectedStreams.get(session.uuid)
                measurementStream = sortedByDetailedType.find { it.detailedType == savedStreamDetailedType }
                    ?: sortedByDetailedType.find { it.detailedType == SensorName.PM2_5.detailedType }
                    ?: session.streams.firstOrNull()
            }
            mSessionPresenter.select(measurementStream)

            val sensorThresholds = mSessionsViewModel.findOrCreateSensorThresholds(session).first()
            mSessionPresenter.setSensorThresholds(sensorThresholds)

            onSessionChangedCallback.invoke()
        }
    }
}
