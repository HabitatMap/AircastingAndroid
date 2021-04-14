package io.lunarlogic.aircasting.screens.dashboard

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.events.DeleteSessionEvent
import io.lunarlogic.aircasting.events.DeleteStreamsEvent
import io.lunarlogic.aircasting.events.ExportSessionEvent
import io.lunarlogic.aircasting.events.UpdateSessionEvent
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.NavigationController
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.lib.ShareHelper
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.screens.session_view.graph.GraphActivity
import io.lunarlogic.aircasting.screens.session_view.map.MapActivity
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.networking.services.*
import org.greenrobot.eventbus.EventBus


abstract class SessionsController(
    protected val mRootActivity: FragmentActivity?,
    private val mViewMvc: SessionsViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    private val mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    val fragmentManager: FragmentManager,
    private val context: Context?
) : SessionsViewMvc.Listener, EditSessionBottomSheet.Listener, ShareSessionBottomSheet.Listener, DeleteSessionBottomSheet.Listener {
    protected val mErrorHandler = ErrorHandler(mRootActivity!!)
    private val mApiService =  mApiServiceFactory.get(mSettings.getAuthToken()!!)

    protected val mMobileSessionsSyncService = SessionsSyncService.get(mApiService, mErrorHandler, mSettings)
    private val mDownloadMeasurementsService = DownloadMeasurementsService(mApiService, mErrorHandler)
    private val mDownloadService = SessionDownloadService(mApiService, mErrorHandler)
    private val mSessionRepository = SessionsRepository()

    protected var editDialog: EditSessionBottomSheet? = null
    protected var shareDialog: ShareSessionBottomSheet? = null
    protected var deleteSessionDialog: DeleteSessionBottomSheet? = null

    protected abstract fun registerSessionsObserver()
    protected abstract fun unregisterSessionsObserver()

    fun onCreate() {
        mViewMvc.showLoader()
    }

    open fun onResume() {
        mViewMvc.showLoader() //todo: added temporarily
        registerSessionsObserver()
        mViewMvc.registerListener(this)
    }

    open fun onPause() {
        unregisterSessionsObserver()
        mViewMvc.unregisterListener(this)
    }

    protected fun startNewSession(sessionType: Session.Type) {
        NewSessionActivity.start(mRootActivity, sessionType)
    }

    override fun onSwipeToRefreshTriggered() {
        mMobileSessionsSyncService.sync(
            onStartCallback = { mViewMvc.showLoader() },
            finallyCallback = { mViewMvc.hideLoader() }
        )
    }

    override fun onFollowButtonClicked(session: Session) {
        updateFollowedAt(session)
    }

    override fun onUnfollowButtonClicked(session: Session) {
        updateFollowedAt(session)
    }

    private fun updateFollowedAt(session: Session) {
        DatabaseProvider.runQuery {
            mSessionsViewModel.updateFollowedAt(session)
        }
    }

    override fun onMapButtonClicked(session: Session, sensorName: String?) {
        MapActivity.start(mRootActivity, sensorName, session.uuid, session.tab)
    }

    override fun onGraphButtonClicked(session: Session, sensorName: String?) {
        GraphActivity.start(mRootActivity, sensorName, session.uuid, session.tab)
    }

    protected fun reloadSession(session: Session) {
        DatabaseProvider.runQuery { scope ->
            val dbSessionWithMeasurements = mSessionsViewModel.reloadSessionWithMeasurements(session.uuid)
            dbSessionWithMeasurements?.let {
                val reloadedSession = Session(dbSessionWithMeasurements)

                DatabaseProvider.backToUIThread(scope) {
                    mViewMvc.reloadSession(reloadedSession)
                    mViewMvc.hideLoaderFor(session)
                }
            }
        }
    }

    override fun onDisconnectSessionClicked(session: Session) {}
    override fun addNoteClicked(session: Session) {}
    override fun onReconnectSessionClicked(session: Session) {}

    override fun onExpandSessionCard(session: Session) {
        mViewMvc.showLoaderFor(session)
        val finallyCallback = { reloadSession(session) }
        mDownloadMeasurementsService.downloadMeasurements(session, finallyCallback)
    }

    override fun onDidYouKnowBoxClicked() {
        NavigationController.goToLetsStart()
    }

    override fun onEditDataPressed(session: Session, name: String, tags: ArrayList<String>) { // handling buttons in EditSessionBottomSheet
        val event = UpdateSessionEvent(session, name, tags)
        EventBus.getDefault().post(event)
    }

    override fun onShareLinkPressed(session: Session, sensor: String) { // handling button in ShareSessionBottomSheet
        if (session.urlLocation != null) {
            openShareIntentChooser(session, sensor)
        } else {
            Toast.makeText(context, context?.getString(R.string.session_upload_pending), Toast.LENGTH_LONG).show()
        }
    }

    override fun onShareFilePressed(session: Session, emailInput: String) { // handling button in ShareSessionBottomSheet
        val event = ExportSessionEvent(session, emailInput)
        EventBus.getDefault().post(event)
    }

    override fun onEditSessionClicked(session: Session) {
        if (!ConnectivityManager.isConnected(context)) {
            Toast.makeText(context, context?.getString(R.string.errors_network_required_edit), Toast.LENGTH_LONG).show()
            return
        }
        val onDownloadSuccess = { session: Session ->
            DatabaseProvider.runQuery {
                mSessionRepository.update(session)
            }
            editDialog?.reload(session)
        }
        val finallyCallback = {
            editDialog?.hideLoader()
        }
        startEditSessionBottomSheet(session)
        mDownloadService.download(session.uuid, onDownloadSuccess, finallyCallback)
    }

    override fun onShareSessionClicked(session: Session) {
        startShareSessionBottomSheet(session)
    }

    override fun onDeleteSessionClicked(session: Session) {
        if (!ConnectivityManager.isConnected(context)) {
            Toast.makeText(context, context?.getString(R.string.errors_network_required_delete_streams), Toast.LENGTH_LONG).show()
            return
        }

        startDeleteSessionBottomSheet(session)
    }

    override fun onDeleteStreamsPressed(session: Session) {
        val allStreamsBoxSelected: Boolean = (deleteSessionDialog?.allStreamsBoxSelected() == true)
        val streamsToDelete = deleteSessionDialog?.getStreamsToDelete()
        if (deleteAllStreamsSelected(allStreamsBoxSelected, streamsToDelete?.size, session.streams.size )) {
            ConfirmationDeleteSessionDialog(this.fragmentManager) {
                deleteSession(session.uuid) }
                .show()
        } else  {
            ConfirmationDeleteSessionDialog(this.fragmentManager) {
                deleteStreams(session, streamsToDelete) }
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

    private fun deleteAllStreamsSelected(allStreamsBoxSelected: Boolean, selectedOptionsCount: Int?, sessionStreamsCount: Int?): Boolean {
        return (allStreamsBoxSelected) || (selectedOptionsCount == sessionStreamsCount)
    }

    private fun startEditSessionBottomSheet(session: Session) {
        editDialog = EditSessionBottomSheet(this, session, context)
        editDialog?.show(fragmentManager)
    }

    private fun startShareSessionBottomSheet(session: Session){
        shareDialog = ShareSessionBottomSheet(this, session, context)
        shareDialog?.show(fragmentManager)
    }

    private fun startDeleteSessionBottomSheet(session: Session) {
        deleteSessionDialog = DeleteSessionBottomSheet(this, session)
        deleteSessionDialog?.show(fragmentManager, "Session delete")
    }

    private fun openShareIntentChooser(session: Session, chosenSensor: String){
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
