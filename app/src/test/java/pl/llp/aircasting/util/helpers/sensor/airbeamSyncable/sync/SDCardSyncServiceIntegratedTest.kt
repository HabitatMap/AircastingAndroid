package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.di.DaggerTestAppComponent
import pl.llp.aircasting.di.TestAppModule
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.DateConverter
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.SyncableAirBeamConfigurator
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.SyncableAirBeamConfiguratorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSyncServiceIntegratedTest.ABMiniStubData.bleCount
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.CSVSession
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileChecker.SDCardCSVFileCheckerFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService.SDCardFileServiceProvider
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.lineParameter.CSVLineParameterHandlerFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor.SDCardFixedSessionsProcessorFactory
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor.SDCardMobileSessionsProcessorFactory
import pl.llp.aircasting.util.sdSyncFinishedCountingIdleResource
import javax.inject.Inject
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

private const val DEVICE_ID = "246f28c47698"

@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [Build.VERSION_CODES.S])
@RunWith(RobolectricTestRunner::class)
class SDCardSyncServiceIntegratedTest {

    @Inject
    lateinit var sdCardFileServiceProvider: SDCardFileServiceProvider

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

    private lateinit var syncableAirBeamConfigurator: SyncableAirBeamConfigurator
    private lateinit var application: AircastingApplication
    private var sessionId by Delegates.notNull<Long>()

    private var pm1Id by Delegates.notNull<Long>()
    private var pm2_5Id by Delegates.notNull<Long>()

    private var pm10Id by Delegates.notNull<Long>()
    private var rhId by Delegates.notNull<Long>()
    private var fId by Delegates.notNull<Long>()

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

    @After
    fun cleanup() {
        IdlingRegistry.getInstance().unregister(sdSyncFinishedCountingIdleResource)
    }

    @Test
    fun `ABMini SD Sync assigns last known location to SD measurements`() = runTest {
        val deviceType = DeviceItem.Type.AIRBEAMMINI
        val sdCardSyncService = setupAndReturnSdCardSyncService(deviceType)
        setupDBWithStubDataABMini()
        val deviceItem: DeviceItem = mock {
            on(it.id) doReturn DEVICE_ID
            on(it.type) doReturn deviceType
        }

        sdCardSyncService.start(mock(), deviceItem)
        syncableAirBeamConfigurator.triggerSDCardDownload()
        waitForSdSyncToFinish()

        measurementsRepository.getAllByStreamId(pm1Id).drop(1).forEach { savedMeasurement ->
            assertEquals(ABMiniDBStub.measurement2.latitude, savedMeasurement.latitude)
            assertEquals(ABMiniDBStub.measurement2.longitude, savedMeasurement.longitude)
        }
        measurementsRepository.getAllByStreamId(pm2_5Id).drop(1).forEach { savedMeasurement ->
            assertEquals(ABMiniDBStub.measurement2.latitude, savedMeasurement.latitude)
            assertEquals(ABMiniDBStub.measurement2.longitude, savedMeasurement.longitude)
        }
    }

    @Test
    fun `AB3 SD Sync takes location of new measurements from SD and retains known location for present measurements`() = runTest {
        val deviceType = DeviceItem.Type.AIRBEAM3
        val sdCardSyncService = setupAndReturnSdCardSyncService(deviceType)
        setupDBWithStubDataAB3()
        val deviceItem: DeviceItem = mock {
            on(it.id) doReturn DEVICE_ID
            on(it.type) doReturn deviceType
        }

        sdCardSyncService.start(mock(), deviceItem)
        syncableAirBeamConfigurator.triggerSDCardDownload()
        waitForSdSyncToFinish()

        val pm2_5measurementFromActiveSession = measurementsRepository.getAllByStreamId(pm2_5Id).first()
        assertEquals(AB3DBStub.measurement.latitude, pm2_5measurementFromActiveSession.latitude)
        assertEquals(AB3DBStub.measurement.longitude, pm2_5measurementFromActiveSession.longitude)
        measurementsRepository.getAllByStreamId(pm2_5Id).drop(1).forEach { savedMeasurement ->
            assertNotEquals(ABMiniDBStub.measurement1.latitude, savedMeasurement.latitude)
            assertNotEquals(ABMiniDBStub.measurement1.longitude, savedMeasurement.longitude)
        }
        val pm1measurementFromActiveSession = measurementsRepository.getAllByStreamId(pm1Id).first()
        assertEquals(AB3DBStub.measurement.latitude, pm1measurementFromActiveSession.latitude)
        assertEquals(AB3DBStub.measurement.longitude, pm1measurementFromActiveSession.longitude)
        measurementsRepository.getAllByStreamId(pm1Id).drop(1).forEach { savedMeasurement ->
            assertNotEquals(ABMiniDBStub.measurement1.latitude, savedMeasurement.latitude)
            assertNotEquals(ABMiniDBStub.measurement1.longitude, savedMeasurement.longitude)
        }
        val pm10measurementFromActiveSession = measurementsRepository.getAllByStreamId(pm10Id).first()
        assertEquals(AB3DBStub.measurement.latitude, pm10measurementFromActiveSession.latitude)
        assertEquals(AB3DBStub.measurement.longitude, pm10measurementFromActiveSession.longitude)
        measurementsRepository.getAllByStreamId(pm10Id).drop(1).forEach { savedMeasurement ->
            assertNotEquals(ABMiniDBStub.measurement1.latitude, savedMeasurement.latitude)
            assertNotEquals(ABMiniDBStub.measurement1.longitude, savedMeasurement.longitude)
        }
        val rhMeasurementFromActiveSession = measurementsRepository.getAllByStreamId(rhId).first()
        assertEquals(AB3DBStub.measurement.latitude, rhMeasurementFromActiveSession.latitude)
        assertEquals(AB3DBStub.measurement.longitude, rhMeasurementFromActiveSession.longitude)
        measurementsRepository.getAllByStreamId(rhId).drop(1).forEach { savedMeasurement ->
            assertNotEquals(ABMiniDBStub.measurement1.latitude, savedMeasurement.latitude)
            assertNotEquals(ABMiniDBStub.measurement1.longitude, savedMeasurement.longitude)
        }
        val fMeasurementFromActiveSession = measurementsRepository.getAllByStreamId(fId).first()
        assertEquals(AB3DBStub.measurement.latitude, fMeasurementFromActiveSession.latitude)
        assertEquals(AB3DBStub.measurement.longitude, fMeasurementFromActiveSession.longitude)
        measurementsRepository.getAllByStreamId(fId).drop(1).forEach { savedMeasurement ->
            assertNotEquals(ABMiniDBStub.measurement1.latitude, savedMeasurement.latitude)
            assertNotEquals(ABMiniDBStub.measurement1.longitude, savedMeasurement.longitude)
        }
    }

    @Test
    fun `ABMini SD Sync saves measurements to DB`() = runTest {
        val deviceType = DeviceItem.Type.AIRBEAMMINI
        val sdCardSyncService = setupAndReturnSdCardSyncService(deviceType)
        setupDBWithStubDataABMini()
        val deviceItem: DeviceItem = mock {
            on(it.id) doReturn DEVICE_ID
            on(it.type) doReturn deviceType
        }

        sdCardSyncService.start(mock(), deviceItem)
        syncableAirBeamConfigurator.triggerSDCardDownload()
        waitForSdSyncToFinish()

        assertEquals(bleCount, measurementsRepository.getAllByStreamId(pm1Id).size)
        assertEquals(bleCount, measurementsRepository.getAllByStreamId(pm2_5Id).size)
    }


    @Test
    fun `AB3 SD Sync saves measurements to DB`() = runTest {
        val deviceType = DeviceItem.Type.AIRBEAM3
        val sdCardSyncService = setupAndReturnSdCardSyncService(deviceType)
        setupDBWithStubDataAB3()
        val deviceItem: DeviceItem = mock {
            on(it.id) doReturn DEVICE_ID
            on(it.type) doReturn deviceType
        }

        sdCardSyncService.start(mock(), deviceItem)
        syncableAirBeamConfigurator.triggerSDCardDownload()
        waitForSdSyncToFinish()

        assertEquals(AB3StubData.bleCount, measurementsRepository.getAllByStreamId(pm1Id).size)
        assertEquals(AB3StubData.bleCount, measurementsRepository.getAllByStreamId(pm2_5Id).size)
        assertEquals(AB3StubData.bleCount, measurementsRepository.getAllByStreamId(pm10Id).size)
        assertEquals(AB3StubData.bleCount, measurementsRepository.getAllByStreamId(fId).size)
        assertEquals(AB3StubData.bleCount, measurementsRepository.getAllByStreamId(rhId).size)
    }

    private suspend fun setupDBWithStubDataABMini() {
        sessionId = sessionsRepository.insert(ABMiniDBStub.session)
        pm1Id = streamsRepository.insert(sessionId, ABMiniDBStub.pm1Stream)
        pm2_5Id = streamsRepository.insert(sessionId, ABMiniDBStub.pm2_5Stream)
        measurementsRepository.insert(pm1Id, sessionId, ABMiniDBStub.measurement1)
        measurementsRepository.insert(pm2_5Id, sessionId, ABMiniDBStub.measurement1)
        measurementsRepository.insert(pm1Id, sessionId, ABMiniDBStub.measurement2)
        measurementsRepository.insert(pm2_5Id, sessionId, ABMiniDBStub.measurement2)
    }

    private suspend fun waitForSdSyncToFinish() {
        while (!sdSyncFinishedCountingIdleResource.isIdleNow) delay(100)
    }

    private fun TestScope.setupAndReturnSdCardSyncService(deviceType: DeviceItem.Type): SDCardSyncService {
        val lineParameterHandler = CSVLineParameterHandlerFactory.create(deviceType)
        val fileChecker = SDCardCSVFileCheckerFactory.create(deviceType)
        val mobileSessionFileHandler =
            mSDCardSessionFileHandlerMobileFactory.create(lineParameterHandler, fileChecker)
        val sdCardMobileSessionsProcessor = mSDCardMobileSessionsProcessorFactory.create(
            lineParameterHandler,
            mobileSessionFileHandler
        )
        val fixedSessionFileHandler =
            mSDCardSessionFileHandlerFixedFactory.create(lineParameterHandler, fileChecker)
        val sdCardFixedSessionsProcessor = mSDCardFixedSessionsProcessorFactory.create(
            lineParameterHandler,
            fixedSessionFileHandler
        )
        val fileService = sdCardFileServiceProvider.get(deviceType)
        syncableAirBeamConfigurator = syncableAirBeamConfiguratorFactory.create(deviceType)
        return SDCardSyncService(
            mock(),
            mock(),
            this,
            fileService,
            fileChecker,
            sdCardMobileSessionsProcessor,
            sdCardFixedSessionsProcessor,
            mock()
        )
    }

    private suspend fun setupDBWithStubDataAB3() {
        sessionId = sessionsRepository.insert(AB3DBStub.session)
        pm1Id = streamsRepository.insert(sessionId, AB3DBStub.pm1Stream)
        pm2_5Id = streamsRepository.insert(sessionId, AB3DBStub.pm2_5Stream)
        pm10Id = streamsRepository.insert(sessionId, AB3DBStub.pm10Stream)
        rhId = streamsRepository.insert(sessionId, AB3DBStub.rhStream)
        fId = streamsRepository.insert(sessionId, AB3DBStub.fStream)
        measurementsRepository.insert(pm1Id, sessionId, AB3DBStub.measurement)
        measurementsRepository.insert(pm2_5Id, sessionId, AB3DBStub.measurement)
        measurementsRepository.insert(pm10Id, sessionId, AB3DBStub.measurement)
        measurementsRepository.insert(rhId, sessionId, AB3DBStub.measurement)
        measurementsRepository.insert(fId, sessionId, AB3DBStub.measurement)
    }

    object ABMiniDBStub {
        val session = Session(
            uuid = "d90ce4d3-cf8a-42b4-9e0f-1b30a9c2e8f1",
            deviceId = DEVICE_ID,
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
        val measurement1 = Measurement(
            value = 11.0,
            time = DateConverter.fromString(
                "09/15/2023 09:52:48",
                dateFormat = CSVSession.DATE_FORMAT
            )!!,
            latitude = 0.0,
            longitude = 1.0,
        )
        val measurement2 = Measurement(
            value = 11.0,
            time = DateConverter.fromString(
                "09/15/2023 09:52:49",
                dateFormat = CSVSession.DATE_FORMAT
            )!!,
            latitude = 10.0,
            longitude = 5.0,
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

    }

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

    object AB3DBStub {
        val session = Session(
            uuid = "2abf080d-d26f-48ad-920a-d192796c0207",
            deviceId = DEVICE_ID,
            deviceType = DeviceItem.Type.AIRBEAM3,
            mType = Session.Type.MOBILE,
            mName = "Session name",
            mTags = arrayListOf(),
            mStatus = Session.Status.DISCONNECTED,
            mStartTime = DateConverter.fromString(
                "09/18/2023 18:58:01",
                dateFormat = CSVSession.DATE_FORMAT
            )!!,
        )
        val measurement = Measurement(
            value = 11.0,
            time = DateConverter.fromString(
                "09/18/2023 18:58:01",
                dateFormat = CSVSession.DATE_FORMAT
            )!!,
            latitude = 0.0,
            longitude = 1.0,
        )
        val pm2_5Stream = MeasurementStream(
            sensorPackageName = "AirBeam3:246f28c47698",
            sensorName = "AirBeam3-PM2.5",
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
            sensorPackageName = "AirBeam3:246f28c47698",
            sensorName = "AirBeam3-PM1",
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
        val pm10Stream = MeasurementStream(
            sensorPackageName = "AirBeam3:246f28c47698",
            sensorName = "AirBeam3-PM10",
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
        val rhStream = MeasurementStream(
            sensorPackageName = "AirBeam3:246f28c47698",
            sensorName = "AirBeam3-RH",
            measurementType = "Humidity",
            measurementShortType = "RH",
            unitName = "percent",
            unitSymbol = "%",
            thresholdVeryLow = 0,
            thresholdLow = 12,
            thresholdMedium = 35,
            thresholdHigh = 55,
            thresholdVeryHigh = 150,
            deleted = false,
        )
        val fStream = MeasurementStream(
            sensorPackageName = "AirBeam3:246f28c47698",
            sensorName = "AirBeam3-F",
            measurementType = "Temperature",
            measurementShortType = "F",
            unitName = "degrees Fahrenheit",
            unitSymbol = "F",
            thresholdVeryLow = 0,
            thresholdLow = 12,
            thresholdMedium = 35,
            thresholdHigh = 55,
            thresholdVeryHigh = 150,
            deleted = false,
        )
    }

    object AB3StubData {
        const val bleCount = 8
        const val wifiCount = 0
        const val cellCount = 0
        const val measurements1 =
            "1,2abf080d-d26f-48ad-920a-d192796c0207,09/18/2023,18:58:01,50.004713,20.062035,79,26,299,73,14,16,16\n" +
                    "2,2abf080d-d26f-48ad-920a-d192796c0207,09/18/2023,18:58:11,50.004713,20.062035,79,26,299,72,18,20,21\n" +
                    "3,2abf080d-d26f-48ad-920a-d192796c0207,09/18/2023,18:58:12,50.004713,20.062035,79,26,299,72,18,20,21\n" +
                    "4,2abf080d-d26f-48ad-920a-d192796c0207,09/18/2023,18:58:13,50.004713,20.062035,79,26,299,72,17,18,19"
        const val measurements2 =
            "5,2abf080d-d26f-48ad-920a-d192796c0207,09/18/2023,18:58:14,50.004713,20.062035,79,26,299,72,17,18,19\n" +
                    "6,2abf080d-d26f-48ad-920a-d192796c0207,09/18/2023,18:58:15,50.004713,20.062035,79,26,299,72,17,18,19\n" +
                    "7,2abf080d-d26f-48ad-920a-d192796c0207,09/18/2023,18:58:16,50.004713,20.062035,79,26,299,72,16,17,18\n" +
                    "8,2abf080d-d26f-48ad-920a-d192796c0207,09/18/2023,18:58:17,50.004713,20.062035,79,26,299,72,16,17,18"
    }
}