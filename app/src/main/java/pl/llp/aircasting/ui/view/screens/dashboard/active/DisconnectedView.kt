package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.disconnected_view.view.disconnected_view
import kotlinx.android.synthetic.main.disconnected_view.view.disconnected_view_bluetooth_device_description
import kotlinx.android.synthetic.main.disconnected_view.view.disconnected_view_bluetooth_device_finish_button
import kotlinx.android.synthetic.main.disconnected_view.view.disconnected_view_bluetooth_device_header
import kotlinx.android.synthetic.main.disconnected_view.view.disconnected_view_bluetooth_device_reconnect_button
import kotlinx.android.synthetic.main.disconnected_view.view.reconnecting_loader
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.extensions.eventbus
import pl.llp.aircasting.util.extensions.getActivity
import pl.llp.aircasting.util.extensions.gone
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.extensions.startAnimation
import pl.llp.aircasting.util.extensions.stopAnimation
import pl.llp.aircasting.util.extensions.visible
import pl.llp.aircasting.util.helpers.bluetooth.BluetoothManager
import pl.llp.aircasting.util.helpers.sensor.common.connector.AirBeamReconnector
import javax.inject.Inject

class DisconnectedView(
    context: Context,
    rootView: View?,
    supportFragmentManager: FragmentManager
) {
    private val mContext: Context = context
    private val rootActivity: Activity? = mContext.getActivity()
    private val mSupportFragmentManager: FragmentManager = supportFragmentManager
    private var lifecycleScope: LifecycleCoroutineScope? = null

    private val mDisconnectedView: View?
    private val mHeader: TextView?
    private val mDescription: TextView?
    private val mPrimaryButton: Button?
    private val mSecondaryButton: Button?
    private val mReconnectingLoader: ImageView?

    @Inject
    lateinit var airBeamReconnector: AirBeamReconnector

    @Inject
    lateinit var mErrorHandler: ErrorHandler

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    lateinit var session: Session

    init {
        mDisconnectedView = rootView?.disconnected_view
        mHeader = rootView?.disconnected_view_bluetooth_device_header
        mDescription = rootView?.disconnected_view_bluetooth_device_description
        mPrimaryButton = rootView?.disconnected_view_bluetooth_device_reconnect_button
        mSecondaryButton = rootView?.disconnected_view_bluetooth_device_finish_button
        mReconnectingLoader = rootView?.reconnecting_loader

        (rootActivity?.application as? AircastingApplication)?.userDependentComponent?.inject(this)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: AirBeamReconnector.ReconnectionEvent) {
        if (event.sessionUuid == session.uuid) {
            if (event.inProgress)
                showReconnectingLoader()
            else
                hideReconnectingLoader()
        }
    }

    fun show(sessionPresenter: SessionPresenter?) {
        lifecycleScope = mDisconnectedView?.findViewTreeLifecycleOwner()?.lifecycleScope
        session = sessionPresenter?.session ?: return

        eventbus.safeRegister(this)

        if (session.isDisconnectable())
            bindSyncableAirBeam(session)
        else
            bindBluetoothDevice(session)

        mDisconnectedView?.visible()
    }

    fun hide() {
        mDisconnectedView?.gone()
        if (eventbus.isRegistered(this))
            eventbus.unregister(this)
    }

    private fun bindBluetoothDevice(session: Session) {
        mHeader?.text = mContext.getString(R.string.disconnected_view_bluetooth_device_header)
        mDescription?.text =
            mContext.getString(R.string.disconnected_view_bluetooth_device_description)
        mPrimaryButton?.text =
            mContext.getString(R.string.disconnected_view_bluetooth_device_reconnect_button)
        mSecondaryButton?.text =
            mContext.getString(R.string.disconnected_view_bluetooth_device_finish_button)

        mPrimaryButton?.setOnClickListener {
            showReconnectingLoader()
            airBeamReconnector.reconnect(
                session,
                deviceItem = null,
                errorCallback = {
                    mErrorHandler.showError(R.string.errors_airbeam_connection_failed)
                },
                finallyCallback = {
                    lifecycleScope?.launch { hideReconnectingLoader() }
                },
            )
        }
        mSecondaryButton?.setOnClickListener {
            FinishSessionConfirmationDialog(
                mSupportFragmentManager,
                session
            ).show()
        }
    }

    private fun bindSyncableAirBeam(session: Session) {
        mHeader?.text = mContext.getString(R.string.disconnected_view_airbeamSyncable_header)
        mDescription?.text = mContext.getString(R.string.disconnected_view_airbeamSyncable_description)
        mPrimaryButton?.text = mContext.getString(R.string.disconnected_view_airbeamSyncable_sync_button)
        mSecondaryButton?.text =
            mContext.getString(R.string.disconnected_view_airbeamSyncable_finish_button)

        mPrimaryButton?.setOnClickListener {
            FinishAndSyncSessionConfirmationDialog(
                mSupportFragmentManager,
                session
            ).show()
        }
        mSecondaryButton?.setOnClickListener {
            FinishSessionConfirmationDialog(
                mSupportFragmentManager,
                session
            ).show()
        }

        moveLoaderToTopLeft()
    }

    private fun moveLoaderToTopLeft() {
        mReconnectingLoader?.updateLayoutParams<ConstraintLayout.LayoutParams> {
            mReconnectingLoader.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    mContext,
                    R.color.aircasting_blue_400
                )
            )
            mDisconnectedView?.let {
                startToStart = it.id
                topToTop = it.id
                bottomToTop = ConstraintLayout.LayoutParams.UNSET
                bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                endToEnd = ConstraintLayout.LayoutParams.UNSET
                endToStart = ConstraintLayout.LayoutParams.UNSET
            }
        }
    }

    private fun showReconnectingLoader() {
        mReconnectingLoader?.startAnimation()
    }

    private fun hideReconnectingLoader() {
        mReconnectingLoader?.stopAnimation()
    }
}