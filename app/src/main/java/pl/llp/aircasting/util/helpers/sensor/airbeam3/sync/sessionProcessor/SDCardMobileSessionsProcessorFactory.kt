package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.sessionProcessor

import dagger.assisted.AssistedFactory
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardSessionFileHandlerFixed
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.SDCardSessionFileHandlerMobile
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.lineParameter.CSVLineParameterHandler

@AssistedFactory
interface SDCardMobileSessionsProcessorFactory {
    fun create(
        lineParameterHandler: CSVLineParameterHandler,
        fileHandlerMobile: SDCardSessionFileHandlerMobile
    ): SDCardMobileSessionsProcessor
}
@AssistedFactory
interface SDCardFixedSessionsProcessorFactory {
    fun create(
        lineParameterHandler: CSVLineParameterHandler,
        fileHandlerMobile: SDCardSessionFileHandlerFixed
    ): SDCardFixedSessionsProcessor
}