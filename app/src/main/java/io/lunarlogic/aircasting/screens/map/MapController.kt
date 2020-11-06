package io.lunarlogic.aircasting.screens.map

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.events.LocationChanged
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.models.Measurement
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.SensorThreshold
import io.lunarlogic.aircasting.models.Session
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MapController(
    private val rootActivity: AppCompatActivity,
    private val mSessionsViewModel: SessionsViewModel,
    private val mViewMvc: MapViewMvc,
    private val sessionUUID: String,
    private var sensorName: String?
): MapViewMvc.Listener {
    private var mSessionPresenter = SessionPresenter()
    private var mLocateRequested = false

    fun onCreate() {
        EventBus.getDefault().register(this);
        mViewMvc.registerListener(this)

        mSessionsViewModel.loadSessionWithMeasurements(sessionUUID).observe(rootActivity, Observer { sessionDBObject ->
            sessionDBObject?.let {
                val session = Session(sessionDBObject)
                if (session.hasChangedFrom(mSessionPresenter.session)) {
                    onSessionChanged(session)
                }
            }
        })
    }

    private fun onSessionChanged(session: Session) {
        mSessionPresenter.session = session

        DatabaseProvider.runQuery { coroutineScope ->
            var selectedSensorName = sensorName
            if (mSessionPresenter.selectedStream != null) {
                selectedSensorName = mSessionPresenter.selectedStream!!.sensorName
            }

            val measurementStream = session.streams.firstOrNull { it.sensorName == selectedSensorName }
            mSessionPresenter.selectedStream = measurementStream

            val sensorThresholds = mSessionsViewModel.findOrCreateSensorThresholds(session)
            mSessionPresenter.setSensorThresholds(sensorThresholds)

            DatabaseProvider.backToUIThread(coroutineScope) {
                mViewMvc.bindSession(mSessionPresenter, this::onSensorThresholdChanged)
            }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocationChanged) {
        if (mLocateRequested) {
            val location = LocationHelper.lastLocation()
            location?.let { mViewMvc.centerMap(location) }
            mLocateRequested = false
        }
    }

    override fun locateRequested() {
        val location = LocationHelper.lastLocation()
        if (location == null) {
            requestLocation()
        } else {
            mViewMvc.centerMap(location)
        }
    }

    override fun onMeasurementStreamChanged(measurementStream: MeasurementStream) {
        this.sensorName = measurementStream.sensorName
    }

    private fun requestLocation() {
        mLocateRequested = true
        LocationHelper.checkLocationServicesSettings(rootActivity)
    }

    fun onLocationSettingsSatisfied() {
        LocationHelper.start()
    }

    override fun onHLUDialogValidationFailed() {
        val errorMessage = rootActivity.getString(R.string.hlu_thresholds_error)
        val toast = Toast.makeText(rootActivity, errorMessage, Toast.LENGTH_LONG)
        toast.show()
    }

    fun onDestroy() {
        EventBus.getDefault().unregister(this);
        mViewMvc.unregisterListener(this)
    }

    private fun onSensorThresholdChanged(sensorThreshold: SensorThreshold) {
        DatabaseProvider.runQuery {
            mSessionsViewModel.updateSensorThreshold(sensorThreshold)
        }
    }
}
