package pl.llp.aircasting.screens.session_view

import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.data_classes.MeasurementDBObject
import pl.llp.aircasting.database.data_classes.SessionDBObject
import pl.llp.aircasting.database.repositories.MeasurementStreamsRepository
import pl.llp.aircasting.database.repositories.MeasurementsRepository
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.events.NewMeasurementEvent
import pl.llp.aircasting.events.NoteDeletedEvent
import pl.llp.aircasting.events.NoteEditedEvent
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.lib.safeRegister
import pl.llp.aircasting.location.LocationHelper
import pl.llp.aircasting.models.*
import pl.llp.aircasting.models.observers.FixedSessionObserver
import pl.llp.aircasting.models.observers.MobileSessionObserver
import pl.llp.aircasting.networking.services.ApiServiceFactory
import pl.llp.aircasting.networking.services.SessionDownloadService
import pl.llp.aircasting.screens.dashboard.SessionPresenter
import pl.llp.aircasting.screens.dashboard.active.EditNoteBottomSheet
import pl.llp.aircasting.screens.session_view.hlu.HLUValidationErrorToast
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
        val sessionUUID = mSessionPresenter.sessionUUID
        var sessionDBObject: SessionDBObject? = null

        sessionUUID?.let { sessionUUID ->
            sessionDBObject = SessionsRepository().getSessionByUUID(sessionUUID)
        }


        sessionDBObject?.let { session ->
            mSessionPresenter.selectedStream?.let { selectedStream ->
                measurements = loadMeasurementsForStreams(session.id,  mSessionPresenter.session?.streams, selectedStream)
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

    private fun loadMeasurementsForStreams(
        sessionId: Long,
        measurementStreams: List<MeasurementStream>?,
        selectedStream: MeasurementStream
    ): HashMap<String, List<Measurement>> {
        var measurements:  HashMap<String, List<Measurement>> = hashMapOf()

        measurementStreams?.forEach { measurementStream ->
            val streamId =
                MeasurementStreamsRepository().getId(sessionId, measurementStream)

            streamId?.let { streamId ->
                measurements[measurementStream.sensorName] =
                    if (measurementStream == selectedStream) {
                        measurementsList(mMeasurementsRepository.getAllByStreamId(streamId)) // TODO: in this exact moment there is the problem
                    } else {
                        measurementsList( // todo: having only one measurement for <not-chosen> stream we got wrong avg value
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
