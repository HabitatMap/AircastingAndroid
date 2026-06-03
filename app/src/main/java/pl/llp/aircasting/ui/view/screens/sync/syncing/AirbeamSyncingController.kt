package pl.llp.aircasting.ui.view.screens.sync.syncing

import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BaseController
import pl.llp.aircasting.ui.view.screens.common.V2WifiPickerEducationalDialog
import pl.llp.aircasting.util.events.DisconnectExternalSensorsEvent
import pl.llp.aircasting.util.events.V2WifiPickerEducationConfirmedEvent
import pl.llp.aircasting.util.events.V2WifiPickerEducationRequestedEvent
import pl.llp.aircasting.util.events.sdcard.SDCardLinesReadEvent
import pl.llp.aircasting.util.events.sdcard.SDCardSyncFinished
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SDCardSyncError
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository

class AirbeamSyncingController(
    viewMvc: AirbeamSyncingViewMvcImpl?,
    private val mFragmentManager: FragmentManager,
    private val mErrorHandler: ErrorHandler,
    private val v2StateRepository: AirBeamMiniV2StateRepository?,
    private val sessionsRepository: SessionsRepository?,
) : BaseController<AirbeamSyncingViewMvcImpl>(viewMvc) {

    private val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var v2ProgressJob: Job? = null
    private var v2FileSizeJob: Job? = null

    fun registerListener(listener: AirbeamSyncingViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun onBackPressed() {
        EventBus.getDefault().post(DisconnectExternalSensorsEvent())
        mFragmentManager.popBackStack()
    }

    fun onCreate() {
        EventBus.getDefault().safeRegister(this)
        observeV2Progress()
        observeV2FileSize()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        v2ProgressJob?.cancel()
        v2FileSizeJob?.cancel()
        uiScope.cancel()
    }

    /**
     * V1 SD-sync drives progress through `SDCardLinesReadEvent` (per-CSV-step counters).
     * V2 manual sync runs over WiFi (no per-step events) — it pushes 0..100 into
     * [AirBeamMiniV2StateRepository.syncProgress] from the orchestrator's per-block byte
     * counter. Mirror that into the syncing screen's header so the user sees the same
     * percentage no matter which manual-sync entry point they came in through.
     */
    private var initialEstimatedSeconds: Long = 0L

    private fun observeV2Progress() {
        val repo = v2StateRepository ?: return
        v2ProgressJob = uiScope.launch {
            repo.syncProgress.collect { percent ->
                mViewMvc?.updateV2Progress(percent)
                if (initialEstimatedSeconds > 0) {
                    val remainingSeconds = Math.ceil(initialEstimatedSeconds * (100.0 - percent) / 100.0).toLong()
                    mViewMvc?.displayEstimatedTime(Math.max(0L, remainingSeconds))
                }
            }
        }
    }

    private fun observeV2FileSize() {
        val repo = v2StateRepository ?: return
        val sessionsRepo = sessionsRepository ?: return
        v2FileSizeJob = uiScope.launch {
            // Check immediately in case it is already populated in the repository
            val initialFileSize = repo.savedSessionFileSize
            val initialUuid = repo.savedSessionUuid
            if (initialFileSize > 0 && !initialUuid.isNullOrEmpty()) {
                val session = withContext(Dispatchers.IO) {
                    sessionsRepo.getSessionByUUID(initialUuid)
                }
                val seconds = calculateEstimatedSyncTimeSeconds(initialFileSize, session)
                if (seconds > 0) {
                    initialEstimatedSeconds = seconds
                    mViewMvc?.displayEstimatedTime(seconds)
                }
            }

            // Also collect from readyToSyncFileSize flow for updates
            repo.readyToSyncFileSize.collect { fileSize ->
                if (fileSize > 0) {
                    val uuid = repo.savedSessionUuid
                    val session = if (!uuid.isNullOrEmpty()) {
                        withContext(Dispatchers.IO) {
                            sessionsRepo.getSessionByUUID(uuid)
                        }
                    } else {
                        null
                    }
                    val seconds = calculateEstimatedSyncTimeSeconds(fileSize, session)
                    if (seconds > 0) {
                        initialEstimatedSeconds = seconds
                        mViewMvc?.displayEstimatedTime(seconds)
                    }
                }
            }
        }
    }

    companion object {
        fun calculateEstimatedSyncTimeSeconds(fileSizeBytes: Long, session: SessionDBObject?): Long {
            if (fileSizeBytes <= 0L) return 0L
            val isMobileWithUnder1mInterval = session != null &&
                    session.type == Session.Type.MOBILE &&
                    session.measurementInterval != null &&
                    session.measurementInterval < 60
            val measurementBytes = if (isMobileWithUnder1mInterval) 8.4 else 12.0
            val measurementsCount = Math.ceil(fileSizeBytes.toDouble() / measurementBytes).toLong()
            val indicateCalls = Math.ceil(measurementsCount.toDouble() / 30.0).toLong()
            // Calibrated 120 ms per indicate call
            val estimatedSeconds = Math.ceil(indicateCalls * 0.12).toLong()
            return Math.max(1L, estimatedSeconds)
        }
    }

    @Subscribe
    fun onMessageEvent(event: SDCardLinesReadEvent) {
        val step = event.step
        mViewMvc?.updateProgress(step, event.linesRead)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: SDCardSyncFinished) {
        mErrorHandler.handle(SDCardSyncError("finishSync, calling listener"))
        mViewMvc?.finishSync(event.isV2)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: V2WifiPickerEducationRequestedEvent) {
        V2WifiPickerEducationalDialog(mFragmentManager) {
            EventBus.getDefault().post(V2WifiPickerEducationConfirmedEvent())
        }.show()
    }
}
