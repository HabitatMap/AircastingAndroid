package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.sensor.SensorEvent


class DashboardController(
    private val mContext: Context?,
    private val mViewMvc: DashboardViewMvc
) : DashboardViewMvc.Listener {

    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(message: Message) {
            val sensorEvent = message.obj as SensorEvent
            mViewMvc.updateMeasurements(sensorEvent)
        }
    }
    private val mMessanger = Messenger(mHandler)

    fun onStart() {
        mViewMvc.registerListener(this)
    }

    fun onStop() {
        mViewMvc.unregisterListener(this)
    }

    override fun onRecordNewSessionClicked() {
        NewSessionActivity.start(mContext, mMessanger)
    }
}