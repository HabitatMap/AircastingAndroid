package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.sensor.SensorEvent

class DashboardHandler: Handler(Looper.getMainLooper()) {
    override fun handleMessage(message: Message) {
        val sensorEvent = message.obj as SensorEvent
        println(sensorEvent.debug())
    }
}

class DashboardController(
    private val mContext: Context?,
    private val mViewMvc: DashboardViewMvc
) : DashboardViewMvc.Listener {

    private val mMessanger = Messenger(DashboardHandler())

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