package pl.llp.aircasting.ui.view.screens.dashboard

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.R
import pl.llp.aircasting.data.local.repository.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.api.services.*
import pl.llp.aircasting.data.local.DatabaseProvider
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.LocalSession
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
import pl.llp.aircasting.util.showToast


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

    protected val mMobileSessionsSyncService =
        SessionsSyncService.get(mApiService, mErrorHandler, mSettings)
    protected val mDownloadMeasurementsService =
        DownloadMeasurementsService(mApiService, mErrorHandler)
    protected val mDownloadService = SessionDownloadService(mApiService, mErrorHandler)
    protected val mSessionRepository = SessionsRepository()
    protected val mActiveSessionsRepository = ActiveSessionMeasurementsRepository()

    protected var editDialog: EditSessionBottomSheet? = null
    protected var shareDialog: ShareSessionBottomSheet? = null
    protected var deleteSessionDialog: DeleteSessionBottomSheet? = null

    protected abstract fun registerSessionsObserver()
    protected abstract fun unregisterSessionsObserver()

    open fun onCreate() {
        mViewMvc?.showLoader()
    }

    open fun onResume() {
        mViewMvc?.showLoader()
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

    protected fun startNewSession(localSessionType: LocalSession.Type) {
        NewSessionActivity.start(mRootActivity, localSessionType)
    }

    override fun onSwipeToRefreshTriggered() {
        mMobileSessionsSyncService.sync(
            onStartCallback = { mViewMvc?.showLoader() },
            finallyCallback = { mViewMvc?.hideLoader() }
        )
    }

    override fun onFollowButtonClicked(localSession: LocalSession) {
        updateFollowedAt(localSession)

        addFollowedSessionMeasurementsToActiveTable(localSession)
        mSettings.increaseFollowedSessionsNumber()
    }

    override fun onUnfollowButtonClicked(localSession: LocalSession) {
        updateFollowedAt(localSession)

        clearUnfollowedSessionMeasurementsFromActiveTable(localSession)
        mSettings.decreaseFollowedSessionsNumber()
    }

    private fun updateFollowedAt(localSession: LocalSession) {
        DatabaseProvider.runQuery {
            mSessionsViewModel.updateFollowedAt(localSession)
            mSessionsViewModel.updateOrder(localSession.uuid, mSettings.getFollowedSessionsNumber())
        }
    }

    override fun onMapButtonClicked(localSession: LocalSession, sensorName: String?) {
        MapActivity.start(mRootActivity, sensorName, localSession.uuid, localSession.tab)
    }

    override fun onGraphButtonClicked(localSession: LocalSession, sensorName: String?) {
        GraphActivity.start(mRootActivity, sensorName, localSession.uuid, localSession.tab)
    }

    private fun reloadSession(localSession: LocalSession) {
        DatabaseProvider.runQuery { scope ->
            val dbSessionWithMeasurements =
                mSessionsViewModel.reloadSessionWithMeasurements(localSession.uuid)
            dbSessionWithMeasurements?.let {
                val reloadedLocalSession = LocalSession(dbSessionWithMeasurements)

                DatabaseProvider.backToUIThread(scope) {
                    mViewMvc?.reloadSession(reloadedLocalSession)
                    mViewMvc?.hideLoaderFor(localSession)
                }
            }
        }
    }

    override fun onDisconnectSessionClicked(localSession: LocalSession) {}
    override fun addNoteClicked(localSession: LocalSession) {}
    override fun onReconnectSessionClicked(localSession: LocalSession) {}

    override fun onExpandSessionCard(localSession: LocalSession) {
        mViewMvc?.showLoaderFor(localSession)
        val finallyCallback = { reloadSession(localSession) }
        mDownloadMeasurementsService.downloadMeasurements(localSession, finallyCallback)
    }

    override fun onEditDataPressed(
        localSession: LocalSession,
        name: String,
        tags: ArrayList<String>
    ) { // handling buttons in EditSessionBottomSheet
        val event = UpdateSessionEvent(localSession, name, tags)
        EventBus.getDefault().post(event)
    }

    override fun onShareLinkPressed(
        localSession: LocalSession,
        sensor: String
    ) { // handling button in ShareSessionBottomSheet
        if (localSession.urlLocation != null) {
            openShareIntentChooser(localSession, sensor)
        } else {
            mErrorHandler.handleAndDisplay(SessionUploadPendingError())
        }
    }

    override fun onShareFilePressed(
        localSession: LocalSession,
        emailInput: String
    ) { // handling button in ShareSessionBottomSheet
        if (localSession.locationless) {
            shareLocalFile(localSession)
        } else {
            val event = ExportSessionEvent(localSession, emailInput)
            EventBus.getDefault().post(event)
        }
    }

    private fun shareLocalFile(localSession: LocalSession) {
        CSVGenerationService(localSession, context!!, CSVHelper(), mErrorHandler).start()
    }


    override fun onEditSessionClicked(localSession: LocalSession) {
        if (!ConnectivityManager.isConnected(context)) {
            context?.apply {
                showToast(
                    getString(R.string.errors_network_required_edit),
                    Toast.LENGTH_LONG
                )
            }
            return
        }
        val onDownloadSuccess = { localSession: LocalSession ->
            DatabaseProvider.runQuery {
                mSessionRepository.update(localSession)
            }
            editDialog?.reload(localSession)
        }
        val finallyCallback = {
            editDialog?.hideLoader()
        }
        startEditSessionBottomSheet(localSession)
        mDownloadService.download(localSession.uuid, onDownloadSuccess, finallyCallback)
    }

    override fun onShareSessionClicked(localSession: LocalSession) {
        var reloadedLocalSession: LocalSession?
        DatabaseProvider.runQuery { scope ->
            val dbSession = mSessionsViewModel.reloadSessionWithMeasurements(localSession.uuid)
            dbSession?.let {
                reloadedLocalSession = LocalSession(dbSession)
                startShareSessionBottomSheet(reloadedLocalSession ?: localSession)
            }
        }
    }

    override fun onDeleteSessionClicked(localSession: LocalSession) {
        if (!ConnectivityManager.isConnected(context)) {
            context?.apply {
                showToast(
                    getString(R.string.errors_network_required_delete_streams),
                    Toast.LENGTH_LONG
                )
            }
            return
        }

        startDeleteSessionBottomSheet(localSession)
    }

    override fun onDeleteStreamsPressed(localSession: LocalSession) {
        val allStreamsBoxSelected: Boolean = (deleteSessionDialog?.allStreamsBoxSelected() == true)
        val streamsToDelete = deleteSessionDialog?.getStreamsToDelete()
        if (deleteAllStreamsSelected(
                allStreamsBoxSelected,
                streamsToDelete?.size,
                localSession.streams.size
            )
        ) {
            ConfirmationDeleteSessionDialog(this.fragmentManager) {
                deleteSession(localSession.uuid)
            }
                .show()
        } else {
            ConfirmationDeleteSessionDialog(this.fragmentManager) {
                deleteStreams(localSession, streamsToDelete)
            }
                .show()
        }
    }

    private fun deleteSession(sessionUUID: String) {
        val event = DeleteSessionEvent(sessionUUID)
        EventBus.getDefault().post(event)
        deleteSessionDialog?.dismiss()
    }

    private fun deleteStreams(localSession: LocalSession, streamsToDelete: List<MeasurementStream>?) {
        val event = DeleteStreamsEvent(localSession, streamsToDelete)
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

    private fun startEditSessionBottomSheet(localSession: LocalSession) {
        editDialog = EditSessionBottomSheet(this, localSession, context)
        editDialog?.show(fragmentManager)
    }

    private fun startShareSessionBottomSheet(localSession: LocalSession) {
        shareDialog = ShareSessionBottomSheet(this, localSession, context)
        shareDialog?.show(fragmentManager)
    }

    private fun startDeleteSessionBottomSheet(localSession: LocalSession) {
        deleteSessionDialog = DeleteSessionBottomSheet(this, localSession)
        deleteSessionDialog?.show(fragmentManager, "Session delete")
    }

    private fun openShareIntentChooser(localSession: LocalSession, chosenSensor: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, ShareHelper.shareLink(localSession, chosenSensor, context))
            putExtra(Intent.EXTRA_SUBJECT, context?.getString(R.string.share_title))
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, context?.getString(R.string.share_link))
        context?.startActivity(chooser)
    }

    private fun addFollowedSessionMeasurementsToActiveTable(localSession: LocalSession) {
        DatabaseProvider.runQuery {
            val sessionId = mSessionRepository.getSessionIdByUUID(localSession.uuid)
            sessionId?.let {
                mActiveSessionsRepository.loadMeasurementsForStreams(
                    it,
                    localSession.streams,
                    ActiveSessionMeasurementsRepository.MAX_MEASUREMENTS_PER_STREAM_NUMBER
                )
            }
        }
    }

    private fun clearUnfollowedSessionMeasurementsFromActiveTable(localSession: LocalSession) {
        DatabaseProvider.runQuery {
            val sessionId = mSessionRepository.getSessionIdByUUID(localSession.uuid)
            mActiveSessionsRepository.deleteBySessionId(sessionId)
        }
    }

}
