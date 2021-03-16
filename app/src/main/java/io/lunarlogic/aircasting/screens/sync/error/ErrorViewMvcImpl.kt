package io.lunarlogic.aircasting.screens.sync.error

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import kotlinx.android.synthetic.main.fragment_sync_error.view.*

class ErrorViewMvcImpl: BaseObservableViewMvc<ErrorViewMvc.Listener>, ErrorViewMvc {
    private val mDescription: TextView?

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        message: String?
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_sync_error, parent, false)

        mDescription = this.rootView?.error_description
        mDescription?.text = message ?: context.getString(R.string.sync_unknown_error)

        val button = rootView?.ok_button
        button?.setOnClickListener {
            onOkClicked()
        }
    }

    private fun onOkClicked() {
        for (listener in listeners) {
            listener.onErrorViewOkClicked()
        }
    }
}
