package io.lunarlogic.aircasting.screens.map

import androidx.appcompat.app.AppCompatActivity
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.sensor.Measurement
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MapController(
    private val rootActivity: AppCompatActivity,
    private val mViewMvc: MapViewMvc,
    private val sessionUUID: String,
    private val sensorName: String?
) {
    private val mSessionsRepository = SessionsRepository()

    fun onCreate() {
        EventBus.getDefault().register(this);

        DatabaseProvider.runQuery {
            val session = mSessionsRepository.loadSessionAndMeasurementsByUUID(sessionUUID)
            if (session != null) {
                val measurementStream = session.streams.firstOrNull { it.sensorName == sensorName }
                mViewMvc.bindSession(session, measurementStream)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NewMeasurementEvent) {
        // TODO: more checks?
        if (event.sensorName == sensorName) {
            val location = LocationHelper.lastLocation()
            val measurement = Measurement(event, location?.latitude , location?.longitude)

            mViewMvc.addMeasurement(measurement)
        }
    }

    fun onDestroy() {
        EventBus.getDefault().unregister(this);
    }
}
