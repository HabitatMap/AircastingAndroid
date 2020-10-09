package io.lunarlogic.aircasting.screens.map

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import io.lunarlogic.aircasting.events.LocationChanged
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.screens.dashboard.SessionsViewModel
import io.lunarlogic.aircasting.sensor.Measurement
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
    private var mSessionPresenter = SessionPresenter()
    private var mLocateRequested = false

    fun onCreate() {
        EventBus.getDefault().register(this);
        mViewMvc.registerListener(this)

        mSessionsViewModel.loadSessionWithMeasurements(sessionUUID).observe(rootActivity, Observer { sessionDBObject ->
            sessionDBObject?.let {
                val session = Session(sessionDBObject)
                if (session.hasChangedFrom(mSessionPresenter.session)) {
                    mSessionPresenter.session = session

                    var selectedSensorName = sensorName
                    if (mSessionPresenter.selectedStream != null) {
                        selectedSensorName = mSessionPresenter.selectedStream!!.sensorName
                    }

                    val measurementStream = session.streams.firstOrNull { it.sensorName == selectedSensorName }
                    mSessionPresenter.selectedStream = measurementStream

                    mViewMvc.bindSession(mSessionPresenter)
                }
            }
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
}
