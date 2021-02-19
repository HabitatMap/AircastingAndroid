package io.lunarlogic.aircasting.sensor.airbeam3.sync

import android.util.Log
import io.lunarlogic.aircasting.events.sdcard.SDCardClearFinished
import io.lunarlogic.aircasting.lib.safeRegister
import io.lunarlogic.aircasting.sensor.AirBeamConnector
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class SDCardClearService() {
    private val CLEAR_CARD_TAG = "CLEAR_CARD"

    fun run(airBeamConnector: AirBeamConnector) {
        EventBus.getDefault().safeRegister(this)

        airBeamConnector.clearSDCard()
    }

    @Subscribe
    fun onEvent(event: SDCardClearFinished) {
        Log.d(CLEAR_CARD_TAG, "Clear SD card finished")
        showFinishMessage()
    }

    private fun showFinishMessage() {
        val message = "Clear SD card finished"
        EventBus.getDefault().post(SyncEvent(message))
    }
}
