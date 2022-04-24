package pl.llp.aircasting.ui.view.screens.sync.refreshed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import kotlinx.android.synthetic.main.fragment_refreshed_sessions.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.common.BaseObservableViewMvc

class RefreshedSessionsViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?,
    success: Boolean
) : BaseObservableViewMvc<RefreshedSessionsViewMvc.Listener>(), RefreshedSessionsViewMvc {
    private val mHeader: TextView?
    private val mDescription: TextView?
    private val mIcon: ImageView?
    private val mPrimaryButton: Button?
    private val mCancelButton: Button?

    init {
        this.rootView = inflater.inflate(R.layout.fragment_refreshed_sessions, parent, false)
        mHeader = this.rootView?.refreshed_sessions_header
        mDescription = this.rootView?.refreshed_sessions_description
        mIcon = this.rootView?.icon
        mPrimaryButton = this.rootView?.primary_button
        mCancelButton = this.rootView?.cancel_button
        if (success) {
            mHeader?.text = context.getString(R.string.refreshed_sessions_header_successful)
            mDescription?.text =
                context.getString(R.string.refreshed_sessions_description_successful)
            mIcon?.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.connected))
            mPrimaryButton?.setOnClickListener { onContinueClicked() }
            mCancelButton?.visibility = View.GONE
        } else {
            mHeader?.text = context.getString(R.string.refreshed_sessions_header_error)
            mDescription?.text = context.getString(R.string.refreshed_sessions_description_error)
            mIcon?.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.error_icon))

            mPrimaryButton?.text = context.getString(R.string.refreshed_sessions_retry)
            mPrimaryButton?.setOnClickListener { onRetryClicked() }
            mCancelButton?.setOnClickListener { onCancelClicked() }
            mCancelButton?.visibility = View.VISIBLE
        }
    }

    private fun onContinueClicked() {
        for (listener in listeners) {
            listener.refreshedSessionsContinueClicked()
        }
    }

    private fun onRetryClicked() {
        for (listener in listeners) {
            listener.refreshedSessionsRetryClicked()
        }
    }

    private fun onCancelClicked() {
        for (listener in listeners) {
            listener.refreshedSessionsCancelClicked()
        }
    }
}
