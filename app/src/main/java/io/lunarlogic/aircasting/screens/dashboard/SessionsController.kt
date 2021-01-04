package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.events.EditSessionEvent
import io.lunarlogic.aircasting.events.ExportSessionEvent
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.NavigationController
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.lib.ShareHelper
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.DownloadMeasurementsService
import io.lunarlogic.aircasting.networking.services.SessionsSyncService
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.screens.session_view.graph.GraphActivity
import io.lunarlogic.aircasting.screens.session_view.map.MapActivity
import org.greenrobot.eventbus.EventBus


abstract class SessionsController(
    private val mRootActivity: FragmentActivity?,
    private val mViewMvc: SessionsViewMvc,
    private val mSessionsViewModel: SessionsViewModel,
    mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    val fragmentManager: FragmentManager,
    private val context: Context?
) : SessionsViewMvc.Listener,
    EditSessionBottomSheet.Listener,
    ShareSessionBottomSheet.Listener {
    protected val mErrorHandler = ErrorHandler(mRootActivity!!)
    private val mApiService =  mApiServiceFactory.get(mSettings.getAuthToken()!!)

    protected val mMobileSessionsSyncService = SessionsSyncService.get(
        mApiService,
        mErrorHandler,
        mSettings
    )
    private val mDownloadMeasurementsService = DownloadMeasurementsService(
        mApiService,
        mErrorHandler
    )

    protected var editDialog: EditSessionBottomSheet? = null //todo: guess these 2 declarations are quite rough now
    protected var shareDialog: ShareSessionBottomSheet? = null

    protected abstract fun registerSessionsObserver()
    protected abstract fun unregisterSessionsObserver()
    protected abstract fun forceSessionsObserverRefresh()

    fun onCreate() {
        mViewMvc.showLoader()
    }

    fun onResume() {
        registerSessionsObserver()
        mViewMvc.registerListener(this)
    }

    fun onPause() {
        unregisterSessionsObserver()
        mViewMvc.unregisterListener(this)
    }

    protected fun startNewSession(sessionType: Session.Type) {
        NewSessionActivity.start(mRootActivity, sessionType)
    }

    override fun onSwipeToRefreshTriggered() {
        mMobileSessionsSyncService.sync({
            mViewMvc.showLoader()
        }, {
            mViewMvc.hideLoader()
        })
    }

    override fun onFollowButtonClicked(session: Session) {
        updateFollowedAt(session)
        NavigationController.goToDashboard(DashboardPagerAdapter.FOLLOWING_TAB_INDEX)
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
    override fun onReconnectSessionClicked(session: Session) {}

    override fun onExpandSessionCard(session: Session) {
        if (session.isIncomplete()) {
            mViewMvc.showLoaderFor(session)
            val finallyCallback = { reloadSession(session) }
            mDownloadMeasurementsService.downloadMeasurements(session, finallyCallback)
        }
    }

    override fun onEditSessionClicked(session: Session) { // handling button in BottomSheet
        startEditSessionBottomSheet(session)
    }

    override fun onShareSessionClicked(session: Session) { // handling button in BottomSheet
        startShareSessionBottomSheet(session)
    }

    override fun onEditDataPressed() { // handling buttons in EditSessionBottomSheet
        val editedSession = editDialog?.editSession()
        editedSession?.let { session ->
            editSessionEventPost(session)
            forceSessionsObserverRefresh()
        }
        editDialog?.dismiss()
    }

    override fun onCancelPressed() { // handling buttons in EditSessionBottomSheet, ShareSessionBottomSheet
        editDialog?.dismiss()
        shareDialog?.dismiss()
    }

    override fun onShareLinkPressed() { // handling button in ShareSessionBottomSheet
        val session = shareDialog!!.session
        val sensor = shareDialog!!.chosenSensor
        shareLinkPressed(session, sensor)
        shareDialog?.dismiss()
    }

    override fun onShareFilePressed() { // handling button in ShareSessionBottomSheet
        shareDialog?.let {
            val session = shareDialog!!.session
            val email = shareDialog!!.shareFilePressed()
            shareSessionEventPost(session, email)
        }
        shareDialog?.dismiss()
    }

    private fun startEditSessionBottomSheet(session: Session) {
        editDialog = EditSessionBottomSheet(this, session)
        editDialog?.show(fragmentManager, "Session edit")
    }

    private fun editSessionEventPost(session: Session){
        val event = EditSessionEvent(session)
        EventBus.getDefault().post(event)
    }

    private fun shareSessionEventPost(session: Session, email: String){
        val event = ExportSessionEvent(session, email)
        EventBus.getDefault().post(event)
    }

    private fun startShareSessionBottomSheet(session: Session){
        shareDialog = ShareSessionBottomSheet(this, session)
        shareDialog?.show(fragmentManager, "Session share")
    }

    private fun shareLinkPressed(session: Session, chosenSensor: String){
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, ShareHelper.shareLink(session, chosenSensor))
            putExtra(Intent.EXTRA_SUBJECT, context?.getString(R.string.share_title))
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, context?.getString(R.string.share_link))
        context?.startActivity(chooser)
    }
}
