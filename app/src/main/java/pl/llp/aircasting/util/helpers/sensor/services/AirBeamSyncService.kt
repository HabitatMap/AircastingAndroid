package pl.llp.aircasting.util.helpers.sensor.services

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.content.ContextCompat
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.exceptions.AirbeamServiceError
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSessionFileHandlerFixedFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSessionFileHandlerMobileFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSyncService
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSyncServiceFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardUploadFixedMeasurementsServiceFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileChecker.SDCardCSVFileCheckerFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService.SDCardFileServiceProvider
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.lineParameter.CSVLineParameterHandlerFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor.SDCardFixedSessionsProcessorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor.SDCardMobileSessionsProcessorFactory
import javax.inject.Inject

class AirBeamSyncService : AirBeamService() {
    @Inject
    lateinit var airBeamDiscoveryService: AirBeamDiscoveryService

    @Inject
    lateinit var sdCardSyncServiceFactory: SDCardSyncServiceFactory

    @Inject
    lateinit var fixedSessionsProcessorFactory: SDCardFixedSessionsProcessorFactory

    @Inject
    lateinit var mobileSessionsProcessorFactory: SDCardMobileSessionsProcessorFactory

    @Inject
    lateinit var fixedFileHandlerFactory: SDCardSessionFileHandlerFixedFactory

    @Inject
    lateinit var mobileFileHandlerFactory: SDCardSessionFileHandlerMobileFactory

    @Inject
    lateinit var sDCardUploadFixedMeasurementsServiceFactory: SDCardUploadFixedMeasurementsServiceFactory

    @Inject
    lateinit var sDCardFileServiceProvider: SDCardFileServiceProvider

    private lateinit var sdCardSyncService: SDCardSyncService

    companion object {
        const val DEVICE_ITEM_KEY = "inputExtraDeviceItem"
        private const val UUID_EXTRA_KEY = "sessionUuid"

        fun startService(context: Context, deviceItem: DeviceItem, sessionUUID: String?) {
            val startIntent = Intent(context, AirBeamSyncService::class.java)
                .putExtra(DEVICE_ITEM_KEY, deviceItem as Parcelable)
                .putExtra(UUID_EXTRA_KEY, sessionUUID)

            ContextCompat.startForegroundService(context, startIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        (application as AircastingApplication).userDependentComponent?.inject(this)
        intent?.extras?.getParcelable<DeviceItem>(DEVICE_ITEM_KEY)?.let { deviceItem ->
            val csvLineParameterHandler = CSVLineParameterHandlerFactory.create(deviceItem.type)
            val csvFileChecker = SDCardCSVFileCheckerFactory.create(deviceItem.type)
            val sDCardFileService = sDCardFileServiceProvider.get(deviceItem.type)
            val mobileFileHandler =
                mobileFileHandlerFactory.create(csvLineParameterHandler, csvFileChecker, deviceItem.type)
            val fixedFileHandler =
                fixedFileHandlerFactory.create(csvLineParameterHandler, csvFileChecker)
            val mobileSessionsProcessor = mobileSessionsProcessorFactory.create(
                csvLineParameterHandler,
                mobileFileHandler
            )
            val fixedSessionsProcessor = fixedSessionsProcessorFactory.create(
                csvLineParameterHandler,
                fixedFileHandler
            )
            val uploadFixedMeasurementsService = sDCardUploadFixedMeasurementsServiceFactory.create(
                fixedFileHandler,
                csvLineParameterHandler
            )

            sdCardSyncService = sdCardSyncServiceFactory.create(
                mobileSessionsProcessor,
                fixedSessionsProcessor,
                uploadFixedMeasurementsService,
                csvFileChecker,
                sDCardFileService,
                intent.getStringExtra(UUID_EXTRA_KEY)
            )
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun startSensor(intent: Intent?) {
        intent ?: return

        val deviceItem =
            intent.getParcelableExtra<DeviceItem>(AirBeamRecordSessionService.DEVICE_ITEM_KEY)

        if (deviceItem == null) {
            errorHandler.handle(AirbeamServiceError("DeviceItem passed through intent is null"))
        } else {
            connect(deviceItem)
        }
    }

    override fun onConnectionSuccessful(deviceItem: DeviceItem, sessionUUID: String?) {
        val airBeamConnector = mAirBeamConnector
        sdCardSyncService.start(airBeamConnector, deviceItem)
    }
}
