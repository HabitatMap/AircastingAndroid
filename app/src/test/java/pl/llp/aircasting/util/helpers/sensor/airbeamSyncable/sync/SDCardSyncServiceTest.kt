package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.DaggerTestAppComponent
import pl.llp.aircasting.di.TestAppModule
import pl.llp.aircasting.di.mocks.sdSync.TestABMiniConfigurator
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.helpers.sensor.AirBeamConnector
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.SyncableAirBeamConfiguratorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSyncServiceTest.DBStub.ABMiniStubData.bleCount
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.CSVSession
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileChecker.SDCardCSVFileChecker
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileChecker.SDCardCSVFileCheckerFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService.ABMiniSDCardFileService
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService.SDCardFileService
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.lineParameter.CSVLineParameterHandlerFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor.SDCardFixedSessionsProcessor
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor.SDCardFixedSessionsProcessorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor.SDCardMobileSessionsProcessor
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor.SDCardMobileSessionsProcessorFactory
import java.io.File
import javax.inject.Inject
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SDCardSyncServiceTest {

    @Mock
    private lateinit var mobileProcessor: SDCardMobileSessionsProcessor

    @Mock
    private lateinit var fixedProcessor: SDCardFixedSessionsProcessor

    @Mock
    private lateinit var abConnector: AirBeamConnector

    @Captor
    lateinit var onDownloadFinishedCaptor: ArgumentCaptor<(stepByFilePaths: Map<SDCardReader.Step?, List<String>>) -> Unit>

    @Inject
    lateinit var miniFileService: ABMiniSDCardFileService

    @Inject
    lateinit var mSDCardMobileSessionsProcessorFactory: SDCardMobileSessionsProcessorFactory

    @Inject
    lateinit var mSDCardFixedSessionsProcessorFactory: SDCardFixedSessionsProcessorFactory

    @Inject
    lateinit var mSDCardSessionFileHandlerMobileFactory: SDCardSessionFileHandlerMobileFactory

    @Inject
    lateinit var mSDCardSessionFileHandlerFixedFactory: SDCardSessionFileHandlerFixedFactory

    @Inject
    lateinit var syncableAirBeamConfiguratorFactory: SyncableAirBeamConfiguratorFactory

    @Inject
    lateinit var measurementsRepository: MeasurementsRepositoryImpl

    @Inject
    lateinit var streamsRepository: MeasurementStreamsRepository

    @Inject
    lateinit var sessionsRepository: SessionsRepository

    private val filePaths = listOf("filepath1", "filepath2", "filepath3")
    private val files = listOf(File(filePaths[0]), File(filePaths[1]), File(filePaths[2]))
    private lateinit var syncableAirBeamConfigurator: TestABMiniConfigurator
    private lateinit var application: AircastingApplication
    private var sessionId by Delegates.notNull<Long>()
    private var pm1Id by Delegates.notNull<Long>()
    private var pm2_5Id by Delegates.notNull<Long>()
    // TODO: stub SDCardCSVFileFactory?

    @Before
    fun setup() {
        IdlingRegistry.getInstance().register(sdSyncFinishedCountingIdleResource)
        application = ApplicationProvider.getApplicationContext()
        DaggerTestAppComponent.builder()
            .testAppModule(TestAppModule(application))
            .build()
            .testUserComponentFactory()
            .create()
            .inject(this)
    }

    @Test
    fun start_integratedEndToEndTestWithDB() = runTest {
        val sdCardSyncService = setupAndReturnSdCardSyncServiceMini()
        setupDBWithStubDataMini()
        val deviceItem: DeviceItem = mock {
            on(it.id) doReturn "246f28c47698"
            on(it.type) doReturn DeviceItem.Type.AIRBEAMMINI
        }

        sdCardSyncService.start(mock(), deviceItem)
        syncableAirBeamConfigurator.triggerSDCardDownload()
        while(!sdSyncFinishedCountingIdleResource.isIdleNow) delay(100)

        assertEquals(bleCount, measurementsRepository.getAllByStreamId(pm1Id).size)
    }

    private suspend fun setupDBWithStubDataMini() {
        sessionId = sessionsRepository.insert(DBStub.session)
        pm1Id = streamsRepository.insert(sessionId, DBStub.pm1Stream)
        pm2_5Id = streamsRepository.insert(sessionId, DBStub.pm2_5Stream)
        measurementsRepository.insert(pm1Id, sessionId, DBStub.measurement)
        measurementsRepository.insert(pm2_5Id, sessionId, DBStub.measurement)
    }

    private fun TestScope.setupAndReturnSdCardSyncServiceMini(): SDCardSyncService {
        val deviceType = DeviceItem.Type.AIRBEAMMINI
        val miniLineParameterHandler = CSVLineParameterHandlerFactory.create(deviceType)
        val miniFileChecker = SDCardCSVFileCheckerFactory.create(deviceType)
        val mobileSessionFileHandler =
            mSDCardSessionFileHandlerMobileFactory.create(miniLineParameterHandler, miniFileChecker)
        val sdCardMobileSessionsProcessor = mSDCardMobileSessionsProcessorFactory.create(
            miniLineParameterHandler,
            mobileSessionFileHandler
        )
        val fixedSessionFileHandler =
            mSDCardSessionFileHandlerFixedFactory.create(miniLineParameterHandler, miniFileChecker)
        val sdCardFixedSessionsProcessor = mSDCardFixedSessionsProcessorFactory.create(
            miniLineParameterHandler,
            fixedSessionFileHandler
        )
        syncableAirBeamConfigurator =
            syncableAirBeamConfiguratorFactory.create(deviceType) as TestABMiniConfigurator
        return SDCardSyncService(
            mock(),
            mock(),
            this,
            miniFileService,
            miniFileChecker,
            sdCardMobileSessionsProcessor,
            sdCardFixedSessionsProcessor,
            mock()
        )
    }

    @Test
    fun start_whenFilesAreNotCorrupted_savesMobileMeasurements_inCorrectOrder() = runTest {
        val mobileStep = SDCardReader.Step(SDCardReader.StepType.MOBILE, 3)
        val stepsByPaths: Map<SDCardReader.Step?, List<String>> = mapOf(
            mobileStep to filePaths
        )
        val fileService = mock<SDCardFileService> {
            on { this.setup(capture(onDownloadFinishedCaptor)) } doAnswer {
                val finishedCallback = onDownloadFinishedCaptor.value
                finishedCallback.invoke(stepsByPaths)
            }
        }
        val fileChecker = mock<SDCardCSVFileChecker> {
            on { areFilesCorrupted(any()) } doReturn false
        }
        val deviceItem = mock<DeviceItem> {
            on { id } doReturn "id"
        }
        val syncService = mock<SessionsSyncService>()
        val service = SDCardSyncService(
            syncService,
            mock(),
            this,
            fileService,
            fileChecker,
            mobileProcessor,
            fixedProcessor,
            mock()
        )

        service.start(abConnector, deviceItem)
        advanceUntilIdle()

        inOrder(abConnector, fileChecker, mobileProcessor, syncService) {
            verify(fileChecker).areFilesCorrupted(stepsByPaths)
            verify(abConnector).clearSDCard()
            advanceUntilIdle()
            verify(mobileProcessor, times(files.size)).start(
                argThat { files.contains(this) },
                eq("id")
            )
            verify(syncService).sync()
        }
    }

    @Test
    fun start_whenFilesAreCorrupted_terminatesSync_doesNotClearSDCard_doesNotProcessMeasurements() =
        runTest {
            val mobileStep = SDCardReader.Step(SDCardReader.StepType.MOBILE, 3)
            val stepsByPaths: Map<SDCardReader.Step?, List<String>> = mapOf(
                mobileStep to filePaths
            )
            val fileService = mock<SDCardFileService> {
                on { this.setup(capture(onDownloadFinishedCaptor)) } doAnswer {
                    val finishedCallback = onDownloadFinishedCaptor.value
                    finishedCallback.invoke(stepsByPaths)
                }
            }
            val fileChecker = mock<SDCardCSVFileChecker> {
                on { areFilesCorrupted(any()) } doReturn true
            }
            val service = SDCardSyncService(
                mock(),
                mock(),
                this,
                fileService,
                fileChecker,
                mobileProcessor,
                fixedProcessor,
                mock()
            )

            service.start(abConnector, mock())

            advanceUntilIdle()
            verify(abConnector, never()).clearSDCard()
            verify(mobileProcessor, never()).start(argThat { files.contains(this) }, eq("id"))
            verify(fileChecker).areFilesCorrupted(stepsByPaths)
        }

    @Test
    fun start_whenFilesAreNotCorrupted_savesFixedMeasurementsLocally_uploadsThemToBackend() =
        runTest {
            val fixedStep = SDCardReader.Step(SDCardReader.StepType.FIXED_WIFI, 3)
            val stepsByPaths: Map<SDCardReader.Step?, List<String>> = mapOf(
                fixedStep to filePaths
            )
            val fileService = mock<SDCardFileService> {
                on { this.setup(capture(onDownloadFinishedCaptor)) } doAnswer {
                    val finishedCallback = onDownloadFinishedCaptor.value
                    finishedCallback.invoke(stepsByPaths)
                }
            }
            val fileChecker = mock<SDCardCSVFileChecker> {
                on { areFilesCorrupted(any()) } doReturn false
            }
            val deviceItem = mock<DeviceItem> {
                on { id } doReturn "id"
            }
            val uploadFixedService = mock<SDCardUploadFixedMeasurementsService>()
            val service = SDCardSyncService(
                mock(),
                mock(),
                this,
                fileService,
                fileChecker,
                mock(),
                fixedProcessor,
                uploadFixedService
            )

            service.start(abConnector, deviceItem)

            advanceUntilIdle()
            verify(fixedProcessor, times(files.size)).start(
                argThat { files.contains(this) },
                eq("id")
            )
            verify(uploadFixedService, times(files.size)).start(
                argThat { files.contains(this) },
                eq("id")
            )
        }

    object DBStub {
        val session = Session(
            uuid = "d90ce4d3-cf8a-42b4-9e0f-1b30a9c2e8f1",
            deviceId = "246f28c47698",
            deviceType = DeviceItem.Type.AIRBEAMMINI,
            mType = Session.Type.MOBILE,
            mName = "Session name",
            mTags = arrayListOf(),
            mStatus = Session.Status.DISCONNECTED,
            mStartTime = DateConverter.fromString(
                "09/15/2023 09:52:48",
                dateFormat = CSVSession.DATE_FORMAT
            )!!,
        )
        val measurement = Measurement(
            value = 11.0,
            time = DateConverter.fromString(
                "09/15/2023 09:52:48",
                dateFormat = CSVSession.DATE_FORMAT
            )!!,
            latitude = 0.0,
            longitude = 1.0,
        )
        val pm2_5Stream = MeasurementStream(
            sensorPackageName = "AirBeamMini:246f28c47698",
            sensorName = "AirBeamMini-PM2.5",
            measurementType = "Particulate Matter",
            measurementShortType = "PM",
            unitName = "micrograms per cubic meter",
            unitSymbol = "µg/m³",
            thresholdVeryLow = 0,
            thresholdLow = 12,
            thresholdMedium = 35,
            thresholdHigh = 55,
            thresholdVeryHigh = 150,
            deleted = false,
        )
        val pm1Stream = MeasurementStream(
            sensorPackageName = "AirBeamMini:246f28c47698",
            sensorName = "AirBeamMini-PM1",
            measurementType = "Particulate Matter",
            measurementShortType = "PM",
            unitName = "micrograms per cubic meter",
            unitSymbol = "µg/m³",
            thresholdVeryLow = 0,
            thresholdLow = 12,
            thresholdMedium = 35,
            thresholdHigh = 55,
            thresholdVeryHigh = 150,
            deleted = false,
        )

        object ABMiniStubData {
            const val bleCount = 7
            const val wifiCount = 0
            const val measurements1 = "d90ce4d3-cf8a-42b4-9e0f-1b30a9c2e8f1\n" +
                    "09/15/2023,09:52:48,11,11\n" +
                    "09/15/2023,09:52:49,10,10\n" +
                    "09/15/2023,09:52:50,10,10"
            const val measurements2 = "09/15/2023,09:52:51,10,10\n" +
                    "09/15/2023,09:52:52,10,10\n" +
                    "09/15/2023,09:52:53,10,10\n" +
                    "09/15/2023,09:52:54,10,10"
        }
    }
}