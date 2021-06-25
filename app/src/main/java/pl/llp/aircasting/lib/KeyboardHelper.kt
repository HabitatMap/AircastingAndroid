package pl.llp.aircasting.lib

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

class KeyboardHelper {
    companion object {
        fun hideKeyboard(context: Context?) {
            val inputManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            // check if no view has focus
            val view = (context as Activity).currentFocus ?: return

            inputManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
