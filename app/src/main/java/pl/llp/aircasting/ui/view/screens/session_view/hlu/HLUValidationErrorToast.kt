package pl.llp.aircasting.ui.view.screens.session_view.hlu

import android.content.Context
import pl.llp.aircasting.R
import pl.llp.aircasting.util.showToast

class HLUValidationErrorToast {
    companion object {
        fun show(context: Context) {
            context.apply { showToast(getString(R.string.hlu_thresholds_error)) }
        }
    }
}
