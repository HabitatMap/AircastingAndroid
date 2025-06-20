package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.capture
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileChecker.SDCardCSVFileChecker
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.csv.fileService.SDCardFileService
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor.SDCardFixedSessionsProcessor
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.sessionProcessor.SDCardMobileSessionsProcessor
import pl.llp.aircasting.util.helpers.sensor.common.connector.AirBeamConnector
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class SDCardSyncServiceTest {
    private val filePaths = listOf("filepath1", "filepath2", "filepath3")
    private val files = listOf(File(filePaths[0]), File(filePaths[1]), File(filePaths[2]))

    @Mock
    private lateinit var mobileProcessor: SDCardMobileSessionsProcessor

    @Mock
    private lateinit var fixedProcessor: SDCardFixedSessionsProcessor

    @Mock
    private lateinit var abConnector: AirBeamConnector

    @Captor
    lateinit var onDownloadFinishedCaptor: ArgumentCaptor<(stepByFilePaths: Map<SDCardReader.Step?, List<String>>) -> Unit>

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
                mock(),
                fileService,
                fileChecker,
                mobileProcessor,
                fixedProcessor,
                mock(),
                "",
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
                mock(),
                fileService,
                fileChecker,
                mock(),
                fixedProcessor,
                uploadFixedService,
                disconnectedSessionUuid = "",
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
}