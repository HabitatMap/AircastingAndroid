package pl.llp.aircasting.ui.view.screens.sync.syncing

import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
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
) : BaseController<AirbeamSyncingViewMvcImpl>(viewMvc) {

    private val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var v2ProgressJob: Job? = null
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
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        v2ProgressJob?.cancel()
        uiScope.cancel()
    }

    /**
     * V1 SD-sync drives progress through `SDCardLinesReadEvent` (per-CSV-step counters).
     * V2 manual sync runs over WiFi (no per-step events) — it pushes 0..100 into
     * [AirBeamMiniV2StateRepository.syncProgress] from the orchestrator's per-block byte
     * counter. Mirror that into the syncing screen's header so the user sees the same
     * percentage no matter which manual-sync entry point they came in through.
     */
    private fun observeV2Progress() {
        val repo = v2StateRepository ?: return
        v2ProgressJob = uiScope.launch {
            repo.syncProgress.collect { percent ->
                mViewMvc?.updateV2Progress(percent)
            }
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
