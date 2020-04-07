package io.lunarlogic.aircasting.screens.common

import android.content.Context
import android.view.View

abstract class BaseViewMvc : ViewMvc {

    override var rootView: View? = null
        protected set

    protected val context: Context
        get() = rootView!!.context

    protected fun <T : View> findViewById(id: Int): T {
        return rootView!!.findViewById(id)
    }
}
