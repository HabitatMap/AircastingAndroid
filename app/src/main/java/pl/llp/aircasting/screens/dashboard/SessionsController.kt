package pl.llp.aircasting.screens.dashboard

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.database.DatabaseProvider
import pl.llp.aircasting.database.repositories.SessionsRepository
import pl.llp.aircasting.events.DeleteSessionEvent
import pl.llp.aircasting.events.DeleteStreamsEvent
import pl.llp.aircasting.events.ExportSessionEvent
import pl.llp.aircasting.events.UpdateSessionEvent
import pl.llp.aircasting.exceptions.ErrorHandler
import pl.llp.aircasting.exceptions.SessionUploadPendingError
import pl.llp.aircasting.lib.CSVHelper
import pl.llp.aircasting.lib.NavigationController
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.lib.ShareHelper
import pl.llp.aircasting.models.MeasurementStream
import pl.llp.aircasting.models.Session
import pl.llp.aircasting.models.SessionsViewModel
import pl.llp.aircasting.networking.services.*
import pl.llp.aircasting.screens.new_session.NewSessionActivity
import pl.llp.aircasting.screens.session_view.graph.GraphActivity
import pl.llp.aircasting.screens.session_view.map.MapActivity
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.database.data_classes.MeasurementDBObject
import pl.llp.aircasting.database.repositories.ActiveSessionMeasurementsRepository
import pl.llp.aircasting.database.repositories.MeasurementStreamsRepository
import pl.llp.aircasting.database.repositories.MeasurementsRepository
import pl.llp.aircasting.models.Measurement


abstract class SessionsController(
    private var mRootActivity: FragmentActivity?,
    private var mViewMvc: SessionsViewMvc?,
    private val mSessionsViewModel: SessionsViewModel,
    private val mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    val fragmentManager: FragmentManager,
    private var context: Context?
) : SessionsViewMvc.Listener, EditSessionBottomSheet.Listener, ShareSessionBottomSheet.Listener, DeleteSessionBottomSheet.Listener {
    protected val mErrorHandler = ErrorHandler(mRootActivity!!)
    private val mApiService =  mApiServiceFactory.get(mSettings.getAuthToken()!!)

    protected val mMobileSessionsSyncService = SessionsSyncService.get(mApiService, mErrorHandler, mSettings)
    protected val mDownloadMeasurementsService = DownloadMeasurementsService(mApiService, mErrorHandler)
    protected val mDownloadService = SessionDownloadService(mApiService, mErrorHandler)
    protected val mSessionRepository = SessionsRepository()
    protected val mActiveSessionsRepository = ActiveSessionMeasurementsRepository()
    protected val mMeasurementsRepository = MeasurementsRepository()

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

    protected fun startNewSession(sessionType: Session.Type) {
        NewSessionActivity.start(mRootActivity, sessionType)
    }

    override fun onSwipeToRefreshTriggered() {
        mMobileSessionsSyncService.sync(
            onStartCallback = { mViewMvc?.showLoader() },
            finallyCallback = { mViewMvc?.hideLoader() }
        )
    }

    override fun onFollowButtonClicked(session: Session) {
        updateFollowedAt(session)
        addFollowedSessionMeasurementsToActiveTable(session)
    }

    override fun onUnfollowButtonClicked(session: Session) {
        updateFollowedAt(session)
        clearUnfollowedSessionMeasurementsFromActiveTable(session)
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
            mErrorHandler.handleAndDisplay(SessionUploadPendingError())
        }
    }

    override fun onShareFilePressed(session: Session, emailInput: String) { // handling button in ShareSessionBottomSheet
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
        var reloadedSession: Session? = null
        DatabaseProvider.runQuery { scope ->
            val dbSession = mSessionsViewModel.reloadSessionWithMeasurements(session.uuid)
            dbSession?.let {
                reloadedSession = Session(dbSession)
                startShareSessionBottomSheet(reloadedSession ?: session)
            }
        }
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

    private fun measurementsList(measurements: List<MeasurementDBObject?>): List<Measurement> {
        return measurements.mapNotNull { measurementDBObject ->
            measurementDBObject?.let { measurement ->
                Measurement(measurement)
            }
        }
    }

    private fun addFollowedSessionMeasurementsToActiveTable(session: Session) {
        DatabaseProvider.runQuery {
            val sessionId = mSessionRepository.getSessionIdByUUID(session.uuid)
            sessionId?.let { loadMeasurementsForStreams(it, session.streams) }
        }
    }

    private fun clearUnfollowedSessionMeasurementsFromActiveTable(session: Session) {
        DatabaseProvider.runQuery {
            val sessionId = mSessionRepository.getSessionIdByUUID(session.uuid)
            mActiveSessionsRepository.deleteBySessionId(sessionId)
        }
    }

    private fun loadMeasurementsForStreams(
        sessionId: Long,
        measurementStreams: List<MeasurementStream>?
    ) {
        var measurements:  List<Measurement> = mutableListOf()

        measurementStreams?.forEach { measurementStream ->
            val streamId =
                MeasurementStreamsRepository().getId(sessionId, measurementStream)

            streamId?.let { streamId ->
                measurements =
                        measurementsList(
                            mMeasurementsRepository.getLastMeasurementsForStream(
                                streamId,
                                540
                            )
                        )
                mActiveSessionsRepository.insertAll(streamId, sessionId, measurements)
            }
        }
    }

}
