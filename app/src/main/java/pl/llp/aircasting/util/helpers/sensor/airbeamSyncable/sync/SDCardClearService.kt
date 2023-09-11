package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync

import android.util.Log
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.util.events.sdcard.SDCardClearFinished
import pl.llp.aircasting.util.helpers.sensor.AirBeamConnector
import pl.llp.aircasting.util.extensions.safeRegister

class SDCardClearService {
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
