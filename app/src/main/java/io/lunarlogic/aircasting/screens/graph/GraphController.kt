package io.lunarlogic.aircasting.screens.graph

import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.models.*
import io.lunarlogic.aircasting.screens.common.hlu.HLUValidationErrorToast
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import kotlinx.coroutines.CoroutineScope
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class GraphController(
    private val rootActivity: AppCompatActivity,
    private val mSessionsViewModel: SessionsViewModel,
    private val mViewMvc: GraphViewMvc,
    private val sessionUUID: String,
    private var sensorName: String?
): GraphViewMvc.Listener {
    private var mSessionPresenter = SessionPresenter(sessionUUID, sensorName)
    private val mSessionObserver = SessionObserver(rootActivity, mSessionsViewModel, mSessionPresenter, this::onSessionChanged)

    fun onCreate() {
        EventBus.getDefault().register(this);
        mViewMvc.registerListener(this)
        mSessionObserver.observe()
    }

    private fun onSessionChanged(coroutineScope: CoroutineScope) {
        DatabaseProvider.backToUIThread(coroutineScope) {
            mViewMvc.bindSession(mSessionPresenter)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NewMeasurementEvent) {
        if (event.sensorName == sensorName) {
            val location = LocationHelper.lastLocation()
            val measurement = Measurement(event, location?.latitude , location?.longitude)

            mViewMvc.addMeasurement(measurement)
        }
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
