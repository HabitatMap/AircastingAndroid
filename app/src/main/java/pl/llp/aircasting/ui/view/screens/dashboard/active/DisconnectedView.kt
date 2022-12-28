package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.disconnected_view.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.util.extensions.gone
import pl.llp.aircasting.util.extensions.startAnimation
import pl.llp.aircasting.util.extensions.stopAnimation
import pl.llp.aircasting.util.extensions.visible

class DisconnectedView(
    context: Context,
    rootView: View?,
    supportFragmentManager: FragmentManager,
    listener: MobileActiveSessionViewMvc.DisconnectedViewListener
) {
    private val mContext: Context = context
    private val mListener: MobileActiveSessionViewMvc.DisconnectedViewListener = listener
    private val mSupportFragmentManager: FragmentManager = supportFragmentManager

    private val mDisconnectedView: View?
    private val mHeader: TextView?
    private val mDescription: TextView?
    private val mPrimaryButton: Button?
    private val mSecondaryButton: Button?
    private val mReconnectingLoader: ImageView?

    init {
        mDisconnectedView = rootView?.disconnected_view
        mHeader = rootView?.disconnected_view_bluetooth_device_header
        mDescription = rootView?.disconnected_view_bluetooth_device_description
        mPrimaryButton = rootView?.disconnected_view_bluetooth_device_reconnect_button
        mSecondaryButton = rootView?.disconnected_view_bluetooth_device_finish_button
        mReconnectingLoader = rootView?.reconnecting_loader
    }

    fun show(sessionPresenter: SessionPresenter?) {
        val session = sessionPresenter?.session
        session ?: return

        if (session.isAirBeam3()) bindAirBeam3(session) else bindBluetoothDevice(session)

        bindReconnectingLoader(sessionPresenter)

        mDisconnectedView?.visible()
    }

    fun hide() {
        mDisconnectedView?.gone()
    }

    private fun bindReconnectingLoader(sessionPresenter: SessionPresenter) {
        if (sessionPresenter.reconnecting) showReconnectingLoader() else hideReconnectingLoader()
    }

    private fun bindBluetoothDevice(session: Session) {
        mHeader?.text = mContext.getString(R.string.disconnected_view_bluetooth_device_header)
        mDescription?.text =
            mContext.getString(R.string.disconnected_view_bluetooth_device_description)
        mPrimaryButton?.text =
            mContext.getString(R.string.disconnected_view_bluetooth_device_reconnect_button)
        mSecondaryButton?.text =
            mContext.getString(R.string.disconnected_view_bluetooth_device_finish_button)

        mPrimaryButton?.setOnClickListener { mListener.onSessionReconnectClicked(session) }
        mSecondaryButton?.setOnClickListener {
            FinishSessionConfirmationDialog(
                mSupportFragmentManager,
                session
            ).show()
        }
    }

    private fun bindAirBeam3(session: Session) {
        mHeader?.text = mContext.getString(R.string.disconnected_view_airbeam3_header)
        mDescription?.text = mContext.getString(R.string.disconnected_view_airbeam3_description)
        mPrimaryButton?.text = mContext.getString(R.string.disconnected_view_airbeam3_sync_button)
        mSecondaryButton?.text =
            mContext.getString(R.string.disconnected_view_airbeam3_finish_button)

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
    }

    private fun showReconnectingLoader() {
        mReconnectingLoader?.startAnimation()
    }

    private fun hideReconnectingLoader() {
        mReconnectingLoader?.stopAnimation()
    }
}
