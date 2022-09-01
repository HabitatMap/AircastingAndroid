package pl.llp.aircasting.ui.view.screens.new_session.session_details

import android.app.Dialog
import android.os.Bundle
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BottomSheet

class WeDoNotLeakPasswordsBottomSheet : BottomSheet() {
    override val EXPANDED_PERCENT = 1.0

    override fun layoutId(): Int {
        return R.layout.we_do_not_leak_passwords_bottom_sheet
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheet = super.onCreateDialog(savedInstanceState)
        expandBottomSheet()
        return bottomSheet
    }
}
