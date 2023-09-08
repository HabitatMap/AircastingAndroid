package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.fileService

import kotlinx.coroutines.CoroutineScope
import pl.llp.aircasting.di.modules.IoCoroutineScope
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.SDCardCSVFileFactory
import javax.inject.Inject

class AB3SDCardFileService @Inject constructor(
    @IoCoroutineScope private val scope: CoroutineScope,
    mCSVFileFactory: SDCardCSVFileFactory,
) : SDCardFileService(scope, mCSVFileFactory) {
    override fun getLine(line: String) = line
    override fun getNewSessionUUID(lineParams: List<String>) = lineParams[1]

    override fun sessionHasChanged(lineParams: List<String>, deviceItem: DeviceItem) =
        lineParams[1] != currentSessionUUID
}