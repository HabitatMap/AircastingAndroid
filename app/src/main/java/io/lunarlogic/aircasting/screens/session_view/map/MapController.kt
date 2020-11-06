package io.lunarlogic.aircasting.screens.session_view.map

import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.events.LocationChanged
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.models.*
import io.lunarlogic.aircasting.screens.session_view.SessionViewController
import io.lunarlogic.aircasting.screens.session_view.SessionViewMvc
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MapController(
    rootActivity: AppCompatActivity,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionViewMvc,
    sessionUUID: String,
    sensorName: String?
): SessionViewController(rootActivity, mSessionsViewModel, mViewMvc, sessionUUID, sensorName) {
    private var mLocateRequested = false

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
}
