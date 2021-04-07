package io.lunarlogic.aircasting.screens.session_view.map

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.database.DatabaseProvider
import io.lunarlogic.aircasting.database.repositories.SessionsRepository
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.models.SessionsViewModel
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import io.lunarlogic.aircasting.networking.services.ConnectivityManager
import io.lunarlogic.aircasting.networking.services.SessionDownloadService
import io.lunarlogic.aircasting.screens.dashboard.active.EditNoteBottomSheet
import io.lunarlogic.aircasting.screens.session_view.SessionDetailsViewMvc

class MapMobileDormantController(
    private val mRootActivity: AppCompatActivity,
    mSessionsViewModel: SessionsViewModel,
    mViewMvc: SessionDetailsViewMvc,
    sessionUUID: String,
    sensorName: String?,
    override val fragmentManager: FragmentManager,
    mApiServiceFactory: ApiServiceFactory,
    mSettings: Settings
): MapController(mRootActivity, mSessionsViewModel, mViewMvc, sessionUUID, sensorName, fragmentManager) {
    protected val mErrorHandler = ErrorHandler(mRootActivity)
    private val mApiService =  mApiServiceFactory.get(mSettings.getAuthToken()!!)
    private val mDownloadService = SessionDownloadService(mApiService, mErrorHandler)
    private val mSessionRepository = SessionsRepository()


    override fun noteMarkerClicked(session: Session?, noteNumber: Int) {
        super.noteMarkerClicked(session, noteNumber)
        editNoteDialog?.showLoader()

        if (!ConnectivityManager.isConnected(mRootActivity)) { //TODO: context replaced with mRootActivity, for now im not sure if it can work
            Toast.makeText(mRootActivity, mRootActivity.getString(R.string.errors_network_required_edit), Toast.LENGTH_LONG).show()
            return
        }
        val onDownloadSuccess = { session: Session ->
            DatabaseProvider.runQuery {
                mSessionRepository.update(session)
            }
            editNoteDialog?.reload(session)
        }
        val finallyCallback = {
            editNoteDialog?.hideLoader()
        }

        session?.let { mDownloadService.download(session.uuid, onDownloadSuccess, finallyCallback) }
    }
}
