package pl.llp.aircasting.ui.view.screens.sync.syncing

import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardReader

interface AirbeamSyncingViewMvc {
    interface Listener {
        fun syncFinished()
    }
    fun updateProgress(step: SDCardReader.Step, linesRead: Int)
}
