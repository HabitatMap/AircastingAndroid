package io.lunarlogic.aircasting.screens.session_view

import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.models.*
import io.lunarlogic.aircasting.models.observers.SessionObserver
import io.lunarlogic.aircasting.screens.session_view.hlu.HLUValidationErrorToast
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import kotlinx.coroutines.CoroutineScope
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


abstract class SessionDetailsViewController(
    protected val rootActivity: AppCompatActivity,
    protected val mSessionsViewModel: SessionsViewModel,
    protected val mViewMvc: SessionDetailsViewMvc,
    val sessionUUID: String,
    private var sensorName: String?
): SessionDetailsViewMvc.Listener {
    private var mSessionPresenter = SessionPresenter(sessionUUID, sensorName)
    private val mSessionObserver = SessionObserver(rootActivity, mSessionsViewModel, mSessionPresenter, this::onSessionChanged)

    fun onCreate() {
        EventBus.getDefault().register(this);
        mViewMvc.registerListener(this)
        DatabaseProvider.runQuery { coroutineScope ->
            val session = mSessionsViewModel.reloadSessionWithMeasurements(sessionUUID)
            DatabaseProvider.backToUIThread(coroutineScope) {
                if (session != null) mSessionPresenter.session = Session(session)
            }
        }
        if(mSessionPresenter.isFixed()) {
            mSessionObserver.observe()
        } else {
            mViewMvc.bindSession(mSessionPresenter)
        }
    }

    private fun onSessionChanged(coroutineScope: CoroutineScope) {
        DatabaseProvider.backToUIThread(coroutineScope) {
            mViewMvc.bindSession(mSessionPresenter)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NewMeasurementEvent) {
        println("MARYSIA: session : ${mSessionPresenter.session}")
        DatabaseProvider.runQuery { coroutineScope ->
            var selectedSensorName = mSessionPresenter.initialSensorName
            if (mSessionPresenter.selectedStream != null) {
                selectedSensorName = mSessionPresenter.selectedStream!!.sensorName
            }

            val measurementStream =
                mSessionPresenter.session?.streams?.firstOrNull { it.sensorName == selectedSensorName }
            mSessionPresenter.selectedStream = measurementStream

            mSessionPresenter?.session?.let { session ->
                val sensorThresholds = mSessionsViewModel.findOrCreateSensorThresholds(session)
                mSessionPresenter.setSensorThresholds(sensorThresholds)
            }
        }

//        if (event.sensorName == mSessionPresenter?.selectedStream?.sensorName) {
            val location = LocationHelper.lastLocation()
            val measurement = Measurement(event, location?.latitude , location?.longitude)
            mViewMvc.bindSession(mSessionPresenter)
            mViewMvc.addMeasurement(measurement, event.sensorName)
//        }
    }

    override fun onSensorThresholdChanged(sensorThreshold: SensorThreshold) {
        DatabaseProvider.runQuery {
            mSessionsViewModel.updateSensorThreshold(sensorThreshold)
        }
    }

    override fun onHLUDialogValidationFailed() {
        HLUValidationErrorToast.show(rootActivity)
    }

    fun onDestroy() {
        EventBus.getDefault().unregister(this);
        mViewMvc.unregisterListener(this)
    }
}
