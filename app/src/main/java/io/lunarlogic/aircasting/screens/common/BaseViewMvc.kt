package io.lunarlogic.aircasting.screens.common

import android.content.Context
import android.view.View
import android.widget.EditText

abstract class BaseViewMvc : ViewMvc {

    override var rootView: View? = null
        protected set

    protected val context: Context
        get() = rootView!!.context

    protected fun <T : View> findViewById(id: Int): T {
        return rootView!!.findViewById(id)
    }

    protected fun getString(id: Int): String? {
        return rootView?.context?.getString(id)
    }

    protected fun getInputValue(id: Int): String {
        val field = rootView?.findViewById<EditText>(id)
        return field?.text.toString()
    }
}
