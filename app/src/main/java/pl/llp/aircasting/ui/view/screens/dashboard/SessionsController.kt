package pl.llp.aircasting.ui.view.screens.dashboard

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.*
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.helpers.SessionFollower
import pl.llp.aircasting.ui.view.screens.new_session.NewSessionActivity
import pl.llp.aircasting.ui.view.screens.session_view.graph.GraphActivity
import pl.llp.aircasting.ui.view.screens.session_view.map.MapActivity
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.CSVHelper
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.ShareHelper
import pl.llp.aircasting.util.events.DeleteSessionEvent
import pl.llp.aircasting.util.events.DeleteStreamsEvent
import pl.llp.aircasting.util.events.ExportSessionEvent
import pl.llp.aircasting.util.events.UpdateSessionEvent
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SessionUploadPendingError
import pl.llp.aircasting.util.extensions.backToUIThread
import pl.llp.aircasting.util.extensions.runOnIOThread
import pl.llp.aircasting.util.extensions.showToast

abstract class SessionsController(
    private var mRootActivity: FragmentActivity?,
    private var mViewMvc: SessionsViewMvc?,
    private val mSessionsViewModel: SessionsViewModel,
    private val mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    val fragmentManager: FragmentManager,
    private var context: Context?
) : SessionsViewMvc.Listener, EditSessionBottomSheet.Listener, ShareSessionBottomSheet.Listener,
    DeleteSessionBottomSheet.Listener {
    protected val mErrorHandler = ErrorHandler(mRootActivity!!)
    private val mApiService = mApiServiceFactory.get(mSettings.getAuthToken()!!)

    private val mDownloadMeasurementsService =
        DownloadMeasurementsService(mApiService, mErrorHandler)
    private val mDownloadService = SessionDownloadService(mApiService, mErrorHandler)
    private val mSessionRepository = SessionsRepository()
    private val mActiveSessionsRepository = ActiveSessionMeasurementsRepository()
    private val sessionFollower =
        SessionFollower(mSettings, mActiveSessionsRepository, mSessionRepository)

    private var editDialog: EditSessionBottomSheet? = null
    private var shareDialog: ShareSessionBottomSheet? = null
    private var deleteSessionDialog: DeleteSessionBottomSheet? = null

    protected abstract fun registerSessionsObserver()
    protected abstract fun unregisterSessionsObserver()

    open fun onCreate() { /* Do nothing */
    }

    open fun onResume() {
        registerSessionsObserver()
        mViewMvc?.registerListener(this)
    }

    open fun onPause() {
        unregisterSessionsObserver()
        mViewMvc?.unregisterListener(this)
    }

    fun onDestroy() {
        unregisterSessionsObserver()
        mViewMvc = null
        mRootActivity = null
        context = null
    }

    protected fun startNewSession(sessionType: Session.Type) {
        NewSessionActivity.start(mRootActivity, sessionType)
    }

    override fun onFollowButtonClicked(session: Session) {
        sessionFollower.follow(session)
    }

    override fun onUnfollowButtonClicked(session: Session) {
        session.resetFollowedAtAndOrder()
        sessionFollower.unfollow(session)
    }

    override fun onMapButtonClicked(session: Session, sensorName: String?) {
        MapActivity.start(mRootActivity, sensorName, session.uuid, session.tab)
    }

    override fun onGraphButtonClicked(session: Session, sensorName: String?) {
        GraphActivity.start(mRootActivity, sensorName, session.uuid, session.tab)
    }

    private fun reloadSession(session: Session) {
        runOnIOThread { scope ->
            val dbSessionWithMeasurements =
                mSessionsViewModel.reloadSessionWithMeasurements(session.uuid)
            dbSessionWithMeasurements?.let {
                val reloadedSession = Session(it)

                backToUIThread(scope) {
                    mViewMvc?.reloadSession(reloadedSession)
                    mViewMvc?.hideLoaderFor(session)
                }
            }
        }
    }

    override fun onDisconnectSessionClicked(session: Session) {}
    override fun addNoteClicked(session: Session) {}
    override fun onReconnectSessionClicked(session: Session) {}

    override fun onExpandSessionCard(session: Session) {
        mViewMvc?.showLoaderFor(session)
        val finallyCallback = { reloadSession(session) }
        mDownloadMeasurementsService.downloadMeasurements(session, finallyCallback)
    }

    override fun onEditDataPressed(
        session: Session,
        name: String,
        tags: ArrayList<String>
    ) { // handling buttons in EditSessionBottomSheet
        val event = UpdateSessionEvent(session, name, tags)
        EventBus.getDefault().post(event)
    }

    override fun onShareLinkPressed(
        session: Session,
        sensor: String
    ) { // handling button in ShareSessionBottomSheet
        if (session.urlLocation != null) {
            openShareIntentChooser(session, sensor)
        } else {
            mErrorHandler.handleAndDisplay(SessionUploadPendingError())
        }
    }

    override fun onShareFilePressed(
        session: Session,
        emailInput: String
    ) { // handling button in ShareSessionBottomSheet
        if (session.locationless) {
            shareLocalFile(session)
        } else {
            val event = ExportSessionEvent(session, emailInput)
            EventBus.getDefault().post(event)
        }
    }

    private fun shareLocalFile(session: Session) {
        CSVGenerationService(session, context!!, CSVHelper(), mErrorHandler).start()
    }


    override fun onEditSessionClicked(session: Session) {
        if (!ConnectivityManager.isConnected(context)) {
            context?.apply {
                showToast(
                    getString(R.string.errors_network_required_edit),
                    Toast.LENGTH_LONG
                )
            }
            return
        }
        startEditSessionBottomSheet(session)
        mRootActivity?.lifecycleScope?.launch {
            mDownloadService.download(session.uuid)
                .onSuccess {
                    withContext(Dispatchers.IO) { mSessionRepository.update(session) }
                    editDialog?.reload(session)
                }
            editDialog?.hideLoader()
        }

    }

    override fun onShareSessionClicked(session: Session) {
        var reloadedSession: Session?
        runOnIOThread { scope ->
            val dbSession = mSessionsViewModel.reloadSessionWithMeasurements(session.uuid)
            dbSession?.let {
                reloadedSession = Session(dbSession)
                startShareSessionBottomSheet(reloadedSession ?: session)
            }
        }
    }

    override fun onDeleteSessionClicked(session: Session) {
        if (!ConnectivityManager.isConnected(context)) {
            context?.apply {
                showToast(
                    getString(R.string.errors_network_required_delete_streams),
                    Toast.LENGTH_LONG
                )
            }
            return
        }

        startDeleteSessionBottomSheet(session)
    }

    override fun onDeleteStreamsPressed(session: Session) {
        val allStreamsBoxSelected: Boolean = (deleteSessionDialog?.allStreamsBoxSelected() == true)
        val streamsToDelete = deleteSessionDialog?.getStreamsToDelete()
        if (deleteAllStreamsSelected(
                allStreamsBoxSelected,
                streamsToDelete?.size,
                session.streams.size
            )
        ) {
            ConfirmationDeleteSessionDialog(this.fragmentManager) {
                deleteSession(session.uuid)
            }
                .show()
        } else {
            ConfirmationDeleteSessionDialog(this.fragmentManager) {
                deleteStreams(session, streamsToDelete)
            }
                .show()
        }
    }

    private fun deleteSession(sessionUUID: String) {
        val event = DeleteSessionEvent(sessionUUID)
        EventBus.getDefault().post(event)
        deleteSessionDialog?.dismiss()
    }

    private fun deleteStreams(session: Session, streamsToDelete: List<MeasurementStream>?) {
        val event = DeleteStreamsEvent(session, streamsToDelete)
        EventBus.getDefault().post(event)
        deleteSessionDialog?.dismiss()
    }

    private fun deleteAllStreamsSelected(
        allStreamsBoxSelected: Boolean,
        selectedOptionsCount: Int?,
        sessionStreamsCount: Int?
    ): Boolean {
        return (allStreamsBoxSelected) || (selectedOptionsCount == sessionStreamsCount)
    }

    private fun startEditSessionBottomSheet(session: Session) {
        editDialog = EditSessionBottomSheet(this, session, context)
        editDialog?.show(fragmentManager)
    }

    private fun startShareSessionBottomSheet(session: Session) {
        shareDialog = ShareSessionBottomSheet(this, session, context)
        shareDialog?.show(fragmentManager)
    }

    private fun startDeleteSessionBottomSheet(session: Session) {
        deleteSessionDialog = DeleteSessionBottomSheet(this, session)
        deleteSessionDialog?.show(fragmentManager, "Session delete")
    }

    private fun openShareIntentChooser(session: Session, chosenSensor: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, ShareHelper.shareLink(session, chosenSensor, context))
            putExtra(Intent.EXTRA_SUBJECT, context?.getString(R.string.share_title))
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, context?.getString(R.string.share_link))
        context?.startActivity(chooser)
    }
}
