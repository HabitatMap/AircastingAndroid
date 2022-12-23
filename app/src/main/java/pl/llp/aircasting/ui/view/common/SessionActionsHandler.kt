package pl.llp.aircasting.ui.view.common

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
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.CSVGenerationService
import pl.llp.aircasting.data.api.services.ConnectivityManager
import pl.llp.aircasting.data.api.services.SessionDownloadService
import pl.llp.aircasting.data.local.repository.SessionsRepository
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.ConfirmationDeleteSessionDialog
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.DeleteSessionBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.EditSessionBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.ShareSessionBottomSheet
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
import pl.llp.aircasting.util.extensions.runOnIOThread
import pl.llp.aircasting.util.extensions.showToast

interface ActionsViewMvcListener {
    fun onEditSessionClicked(session: Session)
    fun onShareSessionClicked(session: Session)
    fun onDeleteSessionClicked(session: Session)
}

abstract class SessionActionsHandler(
    private val mRootActivity: FragmentActivity?,
    private val mErrorHandler: ErrorHandler,
    private val mSessionsViewModel: SessionsViewModel,
    val fragmentManager: FragmentManager,
    private val mSettings: Settings,
    private val mApiService: ApiService,
    private val context: Context?,
    private val mDownloadService: SessionDownloadService,
    private val mSessionRepository: SessionsRepository,
    private var editDialog: EditSessionBottomSheet? = null,
    private var shareDialog: ShareSessionBottomSheet? = null,
    private var deleteSessionDialog: DeleteSessionBottomSheet? = null
) : EditSessionBottomSheet.Listener,
    ShareSessionBottomSheet.Listener,
    DeleteSessionBottomSheet.Listener,
    ActionsViewMvcListener {

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

    override fun onEditSessionClicked(session: Session) {
        startEditSessionBottomSheet(session)
    }

    override fun onShareSessionClicked(session: Session) {
        startShareSessionBottomSheet(session)
    }

    override fun onDeleteSessionClicked(session: Session) {
        startDeleteSessionBottomSheet(session)
    }

    private fun shareLocalFile(session: Session) {
        context ?: return

        CSVGenerationService(session, context, CSVHelper(), mErrorHandler).start()
    }


    fun startEditSessionBottomSheet(session: Session) {
        if (!ConnectivityManager.isConnected(context)) {
            context?.apply {
                showToast(
                    getString(R.string.errors_network_required_edit),
                    Toast.LENGTH_LONG
                )
            }
            return
        }
        showEditSessionBottomSheet(session)
        mRootActivity?.lifecycleScope?.launch {
            mDownloadService.download(session.uuid)
                .onSuccess {
                    withContext(Dispatchers.IO) { mSessionRepository.update(session) }
                    editDialog?.reload(session)
                }
            editDialog?.hideLoader()
        }

    }

    fun startShareSessionBottomSheet(session: Session) {
        var reloadedSession: Session?
        runOnIOThread { scope ->
            val dbSession = mSessionsViewModel.reloadSessionWithMeasurements(session.uuid)
            dbSession?.let {
                reloadedSession = Session(dbSession)
                showShareSessionBottomSheet(reloadedSession ?: session)
            }
        }
    }

    fun startDeleteSessionBottomSheet(session: Session) {
        if (!ConnectivityManager.isConnected(context)) {
            context?.apply {
                showToast(
                    getString(R.string.errors_network_required_delete_streams),
                    Toast.LENGTH_LONG
                )
            }
            return
        }

        showDeleteSessionBottomSheet(session)
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

    private fun showEditSessionBottomSheet(session: Session) {
        editDialog = EditSessionBottomSheet(this, session, context)
        editDialog?.show(fragmentManager)
    }

    private fun showShareSessionBottomSheet(session: Session) {
        shareDialog = ShareSessionBottomSheet(this, session, context)
        shareDialog?.show(fragmentManager)
    }

    private fun showDeleteSessionBottomSheet(session: Session) {
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