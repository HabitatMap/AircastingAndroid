package io.lunarlogic.aircasting.screens.session_view

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.MeasurementStreamsRepository
import io.lunarlogic.aircasting.database.repositories.MeasurementsRepository
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.events.NoteDeletedEvent
import io.lunarlogic.aircasting.events.NoteEditedEvent
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.lib.safeRegister
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.models.*
import io.lunarlogic.aircasting.models.observers.FixedSessionObserver
import io.lunarlogic.aircasting.models.observers.MobileSessionObserver
import io.lunarlogic.aircasting.models.observers.SessionObserver
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.SessionDownloadService
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.screens.dashboard.active.EditNoteBottomSheet
import io.lunarlogic.aircasting.screens.session_view.hlu.HLUValidationErrorToast
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.atomic.AtomicBoolean


abstract class SessionDetailsViewController(
    protected val rootActivity: AppCompatActivity,
    protected val mSessionsViewModel: SessionsViewModel,
    protected var mViewMvc: SessionDetailsViewMvc?,
    sessionUUID: String,
    private var sensorName: String?,
    val fragmentManager: FragmentManager,
    private val mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory
): SessionDetailsViewMvc.Listener,
    EditNoteBottomSheet.Listener {
    private var mSessionPresenter = SessionPresenter(sessionUUID, sensorName)
    private val mSessionObserver = if (mViewMvc?.getSessionType() == Session.Type.FIXED) {
        FixedSessionObserver(
            rootActivity,
            mSessionsViewModel,
            mSessionPresenter,
            this::onSessionChanged
        )
    } else {
        MobileSessionObserver(
            rootActivity,
            mSessionsViewModel,
            mSessionPresenter,
            this::onSessionChanged
        )
    }
    protected var editNoteDialog: EditNoteBottomSheet? = null

    protected val mErrorHandler = ErrorHandler(rootActivity)
    private val mApiService =  mApiServiceFactory.get(mSettings.getAuthToken()!!)
    protected val mDownloadService = SessionDownloadService(mApiService, mErrorHandler)
    protected val mSessionRepository = SessionsRepository()
    private val mMeasurementsRepository = MeasurementsRepository()
    private var mShouldRefreshStatistics = AtomicBoolean(false)

    fun onCreate() {
        EventBus.getDefault().safeRegister(this);
        mViewMvc?.registerListener(this)

        mSessionObserver.observe()
    }

    open fun onResume() {
        mShouldRefreshStatistics.set(true)
    }

    private fun onSessionChanged(coroutineScope: CoroutineScope) {
        DatabaseProvider.backToUIThread(coroutineScope) {
            mViewMvc?.bindSession(mSessionPresenter)
            if (mShouldRefreshStatistics.get()) {
                mViewMvc?.refreshStatisticsContainer()
                mShouldRefreshStatistics.set(false)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NewMeasurementEvent) {
        if (event.sensorName == mSessionPresenter?.selectedStream?.sensorName) {
            val location = LocationHelper.lastLocation()
            val measurement = Measurement(event, location?.latitude , location?.longitude)

            mViewMvc?.bindSession(mSessionPresenter)
            mViewMvc?.addMeasurement(measurement)
        }
    }

    override fun onSensorThresholdChanged(sensorThreshold: SensorThreshold) {
        DatabaseProvider.runQuery {
            mSessionsViewModel.updateSensorThreshold(sensorThreshold)
        }
    }


    override fun onHLUDialogValidationFailed() {
        HLUValidationErrorToast.show(rootActivity)
    }

    fun onDestroy() {
        EventBus.getDefault().unregister(this);
        mViewMvc?.unregisterListener(this)
        mViewMvc = null
    }

    override fun noteMarkerClicked(session: Session?, noteNumber: Int) {
        // TODO: this is not working now on <graph>, displaying note from graph view will be added in "Ready"
        val onDownloadSuccess = { session: Session ->
            DatabaseProvider.runQuery {
                mSessionRepository.update(session)
            }
            editNoteDialog?.reload(session)
        }

        val finallyCallback = {
            editNoteDialog?.hideLoader()
        }

        startEditNoteDialog(session, noteNumber)
        session?.let {
            mDownloadService.download(session.uuid, onDownloadSuccess, finallyCallback)
        }
    }

    fun startEditNoteDialog(session: Session?, noteNumber: Int) {
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
        mSessionPresenter?.session?.streams?.forEach { stream ->
            measurements[stream.sensorName]?.let { streamMeasurements ->
                stream.setMeasurements(streamMeasurements)
            }

        }
    }

    private fun loadMeasurements(): HashMap<String, List<Measurement>> {
        var measurements:  HashMap<String, List<Measurement>> = hashMapOf()
        mSessionPresenter?.let { sessionPresenter ->
            sessionPresenter.sessionUUID?.let { sessionUUID ->
                val sessionDBObject = SessionsRepository().getSessionByUUID(sessionUUID)
                sessionDBObject?.let { session ->
                    sessionPresenter.selectedStream?.let { selectedStream ->
                        sessionPresenter.session?.streams?.forEach { measurementStream ->
                            val streamId =
                                MeasurementStreamsRepository().getId(session.id, measurementStream)

                            streamId?.let { streamId ->
                                measurements[measurementStream.sensorName] = if (measurementStream == selectedStream) {
                                    mMeasurementsRepository.getAllByStreamId(streamId)
                                        .map { measurementDBObject ->
                                            Measurement(measurementDBObject)
                                        }
                                } else {
                                    mMeasurementsRepository.getLastMeasurementsForStream(
                                        streamId,
                                        1
                                    )
                                        .mapNotNull { measurementDBObject ->
                                            measurementDBObject?.let { measurement ->
                                                Measurement(measurement)
                                            }
                                        }
                                }
                            }
                        }
                    }
                }
            }

        }
        return measurements
    }
}
