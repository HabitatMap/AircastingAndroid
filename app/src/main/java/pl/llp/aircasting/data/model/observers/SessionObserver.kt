package pl.llp.aircasting.data.model.observers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pl.llp.aircasting.data.model.Session
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

            val measurementStream =
                session.streams.firstOrNull { it.sensorName == selectedSensorName }
            mSessionPresenter.select(measurementStream)

            val sensorThresholds = mSessionsViewModel.findOrCreateSensorThresholds(session).first()
            mSessionPresenter.setSensorThresholds(sensorThresholds)

            onSessionChangedCallback.invoke()
        }
    }
}
