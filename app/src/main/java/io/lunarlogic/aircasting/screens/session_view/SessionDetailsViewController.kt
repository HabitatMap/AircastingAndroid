package io.lunarlogic.aircasting.screens.session_view

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.events.NewMeasurementEvent
import io.lunarlogic.aircasting.events.NoteDeletedEvent
import io.lunarlogic.aircasting.events.NoteEditedEvent
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.lib.safeRegister
import io.lunarlogic.aircasting.location.LocationHelper
import io.lunarlogic.aircasting.models.*
import io.lunarlogic.aircasting.models.observers.SessionObserver
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.SessionDownloadService
import io.lunarlogic.aircasting.screens.dashboard.SessionPresenter
import io.lunarlogic.aircasting.screens.dashboard.active.EditNoteBottomSheet
import io.lunarlogic.aircasting.screens.session_view.hlu.HLUValidationErrorToast
import kotlinx.coroutines.CoroutineScope
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
    protected var mSessionPresenter = SessionPresenter(sessionUUID, sensorName)
    private val mSessionObserver = SessionObserver(rootActivity, mSessionsViewModel, mSessionPresenter, this::onSessionChanged)
    protected var editNoteDialog: EditNoteBottomSheet? = null

    protected val mErrorHandler = ErrorHandler(rootActivity)
    private val mApiService =  mApiServiceFactory.get(mSettings.getAuthToken()!!)
    protected val mDownloadService = SessionDownloadService(mApiService, mErrorHandler)
    protected val mSessionRepository = SessionsRepository()
    private var mShouldRefresh = AtomicBoolean(false)

    fun onCreate() {
        EventBus.getDefault().safeRegister(this);
        mViewMvc?.registerListener(this)

        mSessionObserver.observe()
    }

    open fun onResume() {
        mShouldRefresh.set(true)
    }

    private fun onSessionChanged(coroutineScope: CoroutineScope) {
        DatabaseProvider.backToUIThread(coroutineScope) {
            mViewMvc?.bindSession(mSessionPresenter)
            if (mShouldRefresh.get()) {
                mViewMvc?.refresh()
                mShouldRefresh.set(false)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NewMeasurementEvent) {
        if (event.sensorName == mSessionPresenter?.selectedStream?.sensorName) {
            val location = LocationHelper.lastLocation()
            val measurement = Measurement(event, location?.latitude , location?.longitude)

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
}
