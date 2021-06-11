package io.lunarlogic.aircasting.models.observers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import kotlinx.coroutines.CoroutineScope

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

        DatabaseProvider.runQuery { coroutineScope ->
            var selectedSensorName = mSessionPresenter.initialSensorName
            if (mSessionPresenter.selectedStream != null) {
                selectedSensorName = mSessionPresenter.selectedStream!!.sensorName
            }

            val measurementStream =
                session.streams.firstOrNull { it.sensorName == selectedSensorName }
            mSessionPresenter.selectedStream = measurementStream

            val sensorThresholds = mSessionsViewModel.findOrCreateSensorThresholds(session)
            mSessionPresenter.setSensorThresholds(sensorThresholds)

            onSessionChangedCallback.invoke(coroutineScope)
        }
    }
}
