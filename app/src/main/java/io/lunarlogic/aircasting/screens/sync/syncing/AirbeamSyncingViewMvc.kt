package io.lunarlogic.aircasting.screens.sync.syncing

import io.lunarlogic.aircasting.sensor.airbeam3.sync.SDCardReader

interface AirbeamSyncingViewMvc {
    fun updateProgress(step: SDCardReader.Step, linesRead: Int)
}
