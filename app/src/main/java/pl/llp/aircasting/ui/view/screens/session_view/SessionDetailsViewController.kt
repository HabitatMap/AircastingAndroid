package pl.llp.aircasting.ui.view.screens.session_view

import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.SessionDownloadService
import pl.llp.aircasting.data.local.entity.MeasurementDBObject
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepositoryImpl
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.*
import pl.llp.aircasting.data.model.observers.FixedSessionObserver
import pl.llp.aircasting.data.model.observers.MobileSessionObserver
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.dashboard.active.EditNoteBottomSheet
import pl.llp.aircasting.ui.view.screens.session_view.hlu.HLUValidationErrorToast
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.NewMeasurementEvent
import pl.llp.aircasting.util.events.NoteDeletedEvent
import pl.llp.aircasting.util.events.NoteEditedEvent
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.*
import pl.llp.aircasting.util.helpers.location.LocationHelper
import java.util.concurrent.atomic.AtomicBoolean

abstract class SessionDetailsViewController(
    protected val mRootActivity: AppCompatActivity,
    protected var mViewMvc: SessionDetailsViewMvc?,
    val fragmentManager: FragmentManager,
    sessionUUID: String,
    sensorName: String?,
    protected val mSessionsViewModel: SessionsViewModel,
    private val mSettings: Settings,
    protected val mErrorHandler: ErrorHandler,
    private val mApiService: ApiService,
    private val mDownloadService: SessionDownloadService,
    private val mSessionRepository: SessionsRepository,
    private val mMeasurementsRepository: MeasurementsRepositoryImpl,
) : SessionDetailsViewMvc.Listener,
    EditNoteBottomSheet.Listener {
    protected var mSessionPresenter = SessionPresenter(sessionUUID, sensorName)
    private val mSessionObserver = if (mViewMvc?.getSessionType() == Session.Type.FIXED) {
        FixedSessionObserver(
            mRootActivity,
            mSessionsViewModel,
            mSessionPresenter,
            this::onSessionChanged
        )
    } else {
        MobileSessionObserver(
            mRootActivity,
            mSessionsViewModel,
            mSessionPresenter,
            this::onSessionChanged
        )
    }
    private var editNoteDialog: EditNoteBottomSheet? = null

    private var mShouldRefreshStatistics = AtomicBoolean(false)

    fun onCreate() {
        EventBus.getDefault().safeRegister(this)
        mViewMvc?.registerListener(this)

        mSessionObserver.observe()
    }

    open fun onResume() {
        mShouldRefreshStatistics.set(true)
        mRootActivity.adjustMenuVisibility(false)
        if (mSettings.isKeepScreenOnEnabled()) mRootActivity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun onSessionChanged(coroutineScope: CoroutineScope) {
        backToUIThread(coroutineScope) {
            mViewMvc?.bindSession(mSessionPresenter)
            if (mShouldRefreshStatistics.get()) {
                mViewMvc?.refreshStatisticsContainer()
                mShouldRefreshStatistics.set(false)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NewMeasurementEvent) {
        if (measurementIsFromSameSessionAndStream(event)) {
            val location = LocationHelper.lastLocation()
            val measurement = Measurement(event, Session.Location.get(location))

            mViewMvc?.bindSession(mSessionPresenter)
            mViewMvc?.addMeasurement(measurement)
        }
    }

    private fun measurementIsFromSameSessionAndStream(event: NewMeasurementEvent) =
        event.sensorName == mSessionPresenter.selectedStream?.sensorName && event.deviceId == mSessionPresenter.session?.deviceId

    override fun onSensorThresholdChanged(sensorThreshold: SensorThreshold) {
        mSessionsViewModel.updateSensorThreshold(sensorThreshold)
    }


    override fun onHLUDialogValidationFailed() {
        HLUValidationErrorToast.show(mRootActivity)
    }

    fun onDestroy() {
        mRootActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        EventBus.getDefault().unregister(this)
        mViewMvc?.unregisterListener(this)
        mViewMvc = null
    }

    override fun noteMarkerClicked(session: Session?, noteNumber: Int) {
        startEditNoteDialog(session, noteNumber)

        session?.let {
            mRootActivity.lifecycleScope.launch {
                mDownloadService.download(session.uuid)
                    .onSuccess {
                        withContext(Dispatchers.IO) { mSessionRepository.update(session) }
                        editNoteDialog?.reload(session)
                    }
                editNoteDialog?.hideLoader()
            }
        }
    }

    private fun startEditNoteDialog(session: Session?, noteNumber: Int) {
        editNoteDialog = EditNoteBottomSheet(this, session, noteNumber)
        editNoteDialog?.show(fragmentManager)
    }

    override fun saveChangesNotePressed(note: Note?, session: Session?) {
        val event = NoteEditedEvent(note, session)
        EventBus.getDefault().post(event)
    }

    override fun deleteNotePressed(note: Note?, session: Session?) {
        val event = NoteDeletedEvent(note, session)
        EventBus.getDefault().post(event)
    }

    override fun refreshSession() {
        if (mSessionPresenter.isFixed()) return
        reloadMeasurements()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun reloadMeasurements() {
        runBlocking {
            val query = GlobalScope.async(Dispatchers.IO) {
                val result = loadMeasurements()
                onMeasurementsLoadResult(result)
            }
            query.await()
        }
    }

    private fun onMeasurementsLoadResult(measurements: HashMap<String, List<Measurement>>) {
        mSessionPresenter.session?.streams?.forEach { stream ->
            measurements[stream.sensorName]?.let { streamMeasurements ->
                stream.setMeasurements(streamMeasurements)
            }

        }
    }

    private suspend fun loadMeasurements(): HashMap<String, List<Measurement>> {
        var measurements: HashMap<String, List<Measurement>> = hashMapOf()
        val sessionUUID = mSessionPresenter.sessionUUID
        var sessionDBObject: SessionDBObject? = null

        sessionUUID?.let { sessionUUID ->
            sessionDBObject = SessionsRepository().getSessionByUUID(sessionUUID)
        }

        sessionDBObject?.let { session ->
            mSessionPresenter.selectedStream?.let { selectedStream ->
                val isSessionDormant =
                    (session.type == Session.Type.MOBILE && session.status == Session.Status.FINISHED)
                measurements = loadMeasurementsForStreams(
                    session.id,
                    mSessionPresenter.session?.streams,
                    selectedStream,
                    isSessionDormant
                )
            }
        }

        return measurements
    }

    private fun measurementsList(measurements: List<MeasurementDBObject?>): List<Measurement> {
        return measurements.mapNotNull { measurementDBObject ->
            measurementDBObject?.let { measurement ->
                Measurement(measurement)
            }
        }
    }

    private suspend fun loadMeasurementsForStreams(
        sessionId: Long,
        measurementStreams: List<MeasurementStream>?,
        selectedStream: MeasurementStream,
        isSessionDormant: Boolean
    ): HashMap<String, List<Measurement>> {
        val measurements: HashMap<String, List<Measurement>> = hashMapOf()

        measurementStreams?.forEach { measurementStream ->
            val streamId =
                MeasurementStreamsRepository().getId(sessionId, measurementStream)

            streamId?.let { streamId ->
                measurements[measurementStream.sensorName] =
                    if (measurementStream == selectedStream || isSessionDormant) {
                        measurementsList(mMeasurementsRepository.getAllByStreamId(streamId))
                    } else {
                        measurementsList(
                            mMeasurementsRepository.getLastMeasurementsForStream(
                                streamId,
                                1
                            )
                        )
                    }
            }
        }

        return measurements
    }
}
