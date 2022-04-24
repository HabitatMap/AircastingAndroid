package pl.llp.aircasting.ui.view.screens.session_view.hlu

import android.content.Context
import android.widget.Toast
import pl.llp.aircasting.R

class HLUValidationErrorToast {
    companion object {
        fun show(context: Context) {
            val errorMessage = context.getString(R.string.hlu_thresholds_error)
            val toast = Toast.makeText(context, errorMessage, Toast.LENGTH_LONG)
            toast.show()
        }
    }
}
