package io.lunarlogic.aircasting.screens.dashboard.active

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.events.StopRecordingEvent
import io.lunarlogic.aircasting.lib.NavigationController
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.common.BaseDialog
import io.lunarlogic.aircasting.screens.dashboard.DashboardPagerAdapter
import kotlinx.android.synthetic.main.disconnected_view_finish_session_dialog.view.*
import org.greenrobot.eventbus.EventBus

class FinishSessionConfirmationDialog(
    mFragmentManager: FragmentManager,
    private val mListener: FinishSessionListener,
    private val mSession: Session
) : BaseDialog(mFragmentManager) {
    private lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.disconnected_view_finish_session_dialog, null)

        mView.informations_text_view.text = buildDescription()
        mView.header.text = buildHeader()

        mView.finish_recording_button.setOnClickListener {
            finishSessionConfirmed()
        }

        mView.cancel_button.setOnClickListener {
            dismiss()
        }

        return mView
    }

    private fun finishSessionConfirmed(){
        mListener.onStopSessionClicked(mSession)
//        val event = StopRecordingEvent(mSession.uuid)
//        EventBus.getDefault().post(event)
//
//        val tabId = DashboardPagerAdapter.tabIndexForSessionType(
//            Session.Type.MOBILE,
//            Session.Status.FINISHED
//        )
//        NavigationController.goToDashboard(tabId)
    }

    private fun buildDescription(): SpannableStringBuilder {
        val blueColor = context?.let {
            ResourcesCompat.getColor(it.resources, R.color.aircasting_blue_400, null)
        } ?: Color.GRAY

        return SpannableStringBuilder()
            .append(getString(R.string.dialog_finish_recording_text_part1))
            .append(" ")
            .color(blueColor, { bold { append(getString(R.string.dialog_finish_recording_text_part2)) } })
            .append(" ")
            .append(getString(R.string.dialog_finish_recording_text_part3))
    }

    private fun buildHeader(): SpannableStringBuilder {
        val blueColor = context?.let{
            ResourcesCompat.getColor(it.resources, R.color.aircasting_blue_400, null)
        } ?: Color.GRAY

        return SpannableStringBuilder()
            .append(getString(R.string.dialog_finish_recording_header_part1))
            .append(" ")
            .color(blueColor, { bold { append(mSession.name) } })
            .append(" ")
            .append(getString(R.string.dialog_finish_recording_header_part3))
    }
}
