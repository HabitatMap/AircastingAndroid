package io.lunarlogic.aircasting.lib

import android.text.TextUtils
import android.util.Patterns

class ValidationHelper {
    companion object{
        fun isValidEmail(target: String): Boolean{
            return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches())
        }
    }
}
