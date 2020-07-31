package io.lunarlogic.aircasting.extensions

import android.content.Context
import android.content.Intent
import io.lunarlogic.aircasting.screens.main.MainActivity

/**
 * Created by Maria Turnau on 31/07/2020.
 */
fun Context.popToRoot()
{
    val intent = Intent(this, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    startActivity(intent)
}
