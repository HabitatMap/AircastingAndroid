package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.core.text.color
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.finish_session_confirmation_dialog.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BaseDialog

open class FinishSessionConfirmationDialog(
    mFragmentManager: FragmentManager,
    protected val mListener: FinishSessionListener,
    protected val mSession: Session
) : BaseDialog(mFragmentManager) {
    private lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.finish_session_confirmation_dialog, null)

        mView.informations_text_view.text = buildDescription()
        mView.header.text = buildHeader()

        mView.finish_recording_button.text = finishButtonText()
        mView.finish_recording_button.setOnClickListener {
            finishSessionConfirmed()
        }

        mView.cancel_button.setOnClickListener {
            dismiss()
        }

        return mView
    }

    protected open fun buildHeader(): SpannableStringBuilder {
        return SpannableStringBuilder()
            .append(getString(R.string.dialog_finish_recording_header_part1))
            .append(" ")
            .color(blueColor()) { bold { append(mSession.name) } }
            .append(getString(R.string.dialog_finish_recording_header_part3))
    }

    protected open fun buildDescription(): SpannableStringBuilder {
        return SpannableStringBuilder()
            .append(getString(R.string.dialog_finish_recording_text_part1))
            .append(" ")
            .color(blueColor()) { bold { append(getString(R.string.dialog_finish_recording_text_part2)) } }
            .append(" ")
            .append(getString(R.string.dialog_finish_recording_text_part3))
    }

    protected open fun finishButtonText(): String? {
        return context?.getString(R.string.finish_recording)
    }

    protected open fun finishSessionConfirmed() {
        mListener.onFinishSessionConfirmed(mSession)
        dismiss()
    }

    protected fun blueColor(): Int {
        return context?.let {
            ResourcesCompat.getColor(it.resources, R.color.aircasting_blue_400, null)
        } ?: Color.GRAY
    }
}
