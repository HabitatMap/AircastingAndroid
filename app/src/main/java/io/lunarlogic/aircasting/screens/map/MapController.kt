package io.lunarlogic.aircasting.screens.map

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.events.LocationChanged
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewModel
import io.lunarlogic.aircasting.sensor.Measurement
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.SensorThreshold
import io.lunarlogic.aircasting.sensor.Session
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MapController(
    private val rootActivity: AppCompatActivity,
    private val mSessionsViewModel: SessionsViewModel,
    private val mViewMvc: MapViewMvc,
    private val sessionUUID: String,
    private val sensorName: String?
): MapViewMvc.Listener {
    private var mSession: Session? = null
    private var mLocateRequested = false
    private var mSensorThreshold: SensorThreshold? = null

    fun onCreate() {
        EventBus.getDefault().register(this);
        mViewMvc.registerListener(this)

        mSessionsViewModel.loadSessionWithMeasurements(sessionUUID).observe(rootActivity, Observer { sessionDBObject ->
            sessionDBObject?.let {
                val session = Session(sessionDBObject)
                if (session.hasChangedFrom(mSession)) {
                    mSession = session
                    val measurementStream = session.streams.firstOrNull { it.sensorName == sensorName }
                    mViewMvc.bindSession(session, measurementStream)

                    bindSensorThreshold(measurementStream)
                }
            }
        })
    }

    private fun bindSensorThreshold(measurementStream: MeasurementStream?) {
        DatabaseProvider.runQueryWithUICallback({
            val sensorThresholdDBObject =
                mSessionsViewModel.findOrCreateSensorThreshold(sensorName, measurementStream)
            sensorThresholdDBObject?.let {
                mSensorThreshold = SensorThreshold(sensorThresholdDBObject)
            }
        }, {
            mViewMvc.bindSensorThreshold(mSensorThreshold, this::onSensorThresholdChanged)
        })
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

    private fun requestLocation() {
        mLocateRequested = true
        LocationHelper.checkLocationServicesSettings(rootActivity)
    }

    fun onLocationSettingsSatisfied() {
        LocationHelper.start()
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
