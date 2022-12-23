package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.text.SpannableStringBuilder
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session

class FinishAndSyncSessionConfirmationDialog(
    mFragmentManager: FragmentManager,
    mListener: FinishSessionListener,
    mSession: Session
) : FinishSessionConfirmationDialog(mFragmentManager, mListener, mSession) {

    override fun buildHeader(): SpannableStringBuilder {
        return SpannableStringBuilder()
            .append(getString(R.string.dialog_finish_and_sync_recording_header_part1))
            .append(" ")
            .color(blueColor(), { bold { append(mSession.name) } })
            .append(" ")
            .append(getString(R.string.dialog_finish_and_sync_recording_header_part3))
    }

    override fun buildDescription(): SpannableStringBuilder {
        return SpannableStringBuilder()
            .append(getString(R.string.dialog_finish_and_sync_recording_text_part1))
            .append(" ")
            .color(blueColor(), { bold { append(getString(R.string.dialog_finish_and_sync_recording_text_part2)) } })
            .append(" ")
            .append(getString(R.string.dialog_finish_and_sync_recording_text_part3))
            .append("\n\n")
            .bold { append(getString(R.string.dialog_finish_and_sync_recording_text_part4)) }
    }

    override fun finishButtonText(): String? {
        return context?.getString(R.string.finish_recording_and_sync)
    }

    override fun finishSessionConfirmed() {
        mListener?.onFinishAndSyncSessionConfirmed(mSession)
        dismiss()
    }
}
