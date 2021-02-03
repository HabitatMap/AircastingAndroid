package io.lunarlogic.aircasting.screens.lets_start

import android.content.Context
import android.widget.Toast
import io.lunarlogic.aircasting.R
import android.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import io.lunarlogic.aircasting.events.LocationPermissionsResultEvent
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.safeRegister
import io.lunarlogic.aircasting.screens.new_session.NewSessionActivity
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.networking.services.ConnectivityManager
import io.lunarlogic.aircasting.permissions.PermissionsManager
import io.lunarlogic.aircasting.sensor.AirBeamSyncService
import io.lunarlogic.aircasting.sensor.airbeam3.AirBeam3Configurator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class LetsStartController(
    private val mRootActivity: FragmentActivity?,
    private val mViewMvc: LetsStartViewMvc,
    private val mContext: Context?,
    private val mPermissionsManager: PermissionsManager,
    private val mErrorHandler: ErrorHandler
): LetsStartViewMvc.Listener {
    private var syncProgressDialog: AlertDialog? = null // TODO: remove it after implementing proper sync

    fun onCreate() {
        mViewMvc.registerListener(this)
        EventBus.getDefault().safeRegister(this)
    }

    fun onDestroy() {
        mViewMvc.unregisterListener(this)
        EventBus.getDefault().unregister(this)
    }

    override fun onFixedSessionSelected() {
        if (!ConnectivityManager.isConnected(mContext)) {
            Toast.makeText(mContext, mContext?.getString(R.string.fixed_session_no_internet_connection), Toast.LENGTH_LONG).show()
            return
        }

        NewSessionActivity.start(mRootActivity, Session.Type.FIXED)
    }

    override fun onMobileSessionSelected() {
        NewSessionActivity.start(mRootActivity, Session.Type.MOBILE)
    }

    override fun onSyncSelected() {
        val rootActivity = mRootActivity ?: return

        if (mPermissionsManager.locationPermissionsGranted(rootActivity)) {
            performSync()
        } else {
            mPermissionsManager.requestLocationPermissions(rootActivity)
            // Sync will be run when user allows needed permissions
            // Check LetsStartController and onRequestPermissionsResult below
        }
    }

    @Subscribe
    fun onMessageEvent(event: LocationPermissionsResultEvent) {
        if (mPermissionsManager.permissionsGranted(event.grantResults)) {
            performSync()
        } else {
            mErrorHandler.showError(R.string.errors_location_services_required_to_sync)
        }
    }

    private fun performSync() {
        mContext ?: return

        AirBeamSyncService.startService(mContext)
        syncProgressDialog = AlertDialog.Builder(mRootActivity)
            .setCancelable(false)
            .setPositiveButton("Ok", null)
            .setMessage("Sync started")
            .show()
    }

    // TODO: remove this method after implementing proper sync
    @Subscribe
    fun onMessageEvent(event: AirBeam3Configurator.SyncEvent) {
        syncProgressDialog?.setMessage(event.message)
    }

    // TODO: remove this method after implementing proper sync
    @Subscribe
    fun onMessageEvent(event: AirBeam3Configurator.SyncFinishedEvent) {
        syncProgressDialog?.cancel()
        syncProgressDialog = AlertDialog.Builder(mRootActivity)
            .setCancelable(false)
            .setPositiveButton("Ok", null)
            .setMessage(event.message)
            .show()
    }

    override fun onClearSDCardSelected() {
        mContext ?: return

        AirBeamSyncService.startService(mContext, true)
        syncProgressDialog = AlertDialog.Builder(mRootActivity).setMessage("Clear SD card started").show()
    }

    override fun onMoreInfoClicked() {
        mViewMvc.showMoreInfoDialog()
    }
}
