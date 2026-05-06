package pl.llp.aircasting.ui.view.screens.common

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.finish_session_confirmation_dialog.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseDialog

/**
 * Shown right before the V2 manual-sync flow opens the system Wi-Fi picker. Tells the user
 * to tap the AirBeam SoftAP entry ("AirBeamMini Sync") and that the picker will close
 * itself once the phone joins. User confirms via a single "Continue" button which invokes
 * [onContinue] — the caller resumes the sync coroutine from there.
 */
class V2WifiPickerEducationalDialog(
    mFragmentManager: FragmentManager,
    private val onContinue: () -> Unit,
) : BaseDialog(mFragmentManager) {

    override fun setupView(inflater: LayoutInflater): View {
        val view = inflater.inflate(R.layout.finish_session_confirmation_dialog, null)

        view.header.text = getString(R.string.dialog_v2_wifi_picker_education_header)
        view.informations_text_view.text =
            Html.fromHtml(getString(R.string.dialog_v2_wifi_picker_education_description), Html.FROM_HTML_MODE_LEGACY)

        view.finish_recording_button.text = getString(R.string.dialog_v2_wifi_picker_education_button)
        view.finish_recording_button.setOnClickListener {
            onContinue()
            dismiss()
        }

        view.cancel_button.visibility = View.GONE

        // Cancel button below carries the layout's bottom margin; with it gone, the
        // Continue button sits flush against the dialog's rounded edge. Mirror that
        // margin onto the visible button so the spacing matches the rest of the dialog.
        val marginPx = resources.getDimensionPixelSize(R.dimen.keyline_6)
        view.finish_recording_button.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = marginPx
        }

        isCancelable = false
        return view
    }
}
