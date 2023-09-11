package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService

import kotlinx.coroutines.CoroutineScope
import pl.llp.aircasting.di.modules.IoCoroutineScope
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.SDCardCSVFileFactory
import javax.inject.Inject

class ABMiniSDCardFileService @Inject constructor(
    @IoCoroutineScope private val scope: CoroutineScope,
    private val mCSVFileFactory: SDCardCSVFileFactory,
) : SDCardFileService(scope, mCSVFileFactory) {
    override fun getLine(line: String) = "$currentSessionUUID,$line"
    override fun getNewSessionUUID(lineParams: List<String>) = lineParams[0]
    override fun sessionHasChanged(lineParams: List<String>, deviceItem: DeviceItem) =
        lineParams.size == 1
}