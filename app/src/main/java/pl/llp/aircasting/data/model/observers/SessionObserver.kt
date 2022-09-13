package pl.llp.aircasting.data.model.observers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.extensions.runOnIOThread

abstract class SessionObserver<Type>(
    private val mLifecycleOwner: LifecycleOwner,
    protected val mSessionsViewModel: SessionsViewModel,
    private val mSessionPresenter: SessionPresenter,
    private val onSessionChangedCallback: (coroutineScope: CoroutineScope) -> Unit

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

        runOnIOThread { coroutineScope ->
            var selectedSensorName = mSessionPresenter.initialSensorName
            if (mSessionPresenter.selectedStream != null) {
                selectedSensorName = mSessionPresenter.selectedStream!!.sensorName
            }

            val measurementStream =
                session.streams.firstOrNull { it.sensorName == selectedSensorName }
            mSessionPresenter.select(measurementStream)

            val sensorThresholds = mSessionsViewModel.findOrCreateSensorThresholds(session)
            mSessionPresenter.setSensorThresholds(sensorThresholds)

            onSessionChangedCallback.invoke(coroutineScope)
        }
    }
}
