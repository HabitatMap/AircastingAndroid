package io.lunarlogic.aircasting.screens.sync.error

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.lunarlogic.aircasting.screens.common.BaseFragment

class ErrorFragment: BaseFragment<ErrorViewMvcImpl, ErrorController>() {
    lateinit var listener: ErrorViewMvc.Listener
    var message: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = ErrorViewMvcImpl(layoutInflater, null, message)
        controller = ErrorController(view)

        return view?.rootView
    }

    override fun onStart() {
        super.onStart()
        controller?.registerListener(listener)
    }

    override fun onStop() {
        super.onStop()
        controller?.unregisterListener(listener)
    }
}
