package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.screens.sync.error.ErrorController
import pl.llp.aircasting.ui.view.screens.sync.error.ErrorViewMvc
import pl.llp.aircasting.ui.view.screens.sync.error.ErrorViewMvcImpl

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
