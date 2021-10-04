package pl.llp.aircasting.screens.sync.syncing

import pl.llp.aircasting.sensor.airbeam3.sync.SDCardReader

interface AirbeamSyncingViewMvc {
    interface Listener {
        fun syncFinished()
    }
    fun updateProgress(step: SDCardReader.Step, linesRead: Int)
}
