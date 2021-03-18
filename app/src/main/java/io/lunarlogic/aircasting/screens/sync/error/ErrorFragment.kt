package io.lunarlogic.aircasting.screens.sync.error

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class ErrorFragment: Fragment() {
    private var controller: ErrorController? = null
    lateinit var listener: ErrorViewMvc.Listener
    var message: String? = null
    private var view: ErrorViewMvcImpl? = null

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

    override fun onDestroyView() {
        super.onDestroyView()
        controller?.onDestroy()
        controller = null
        view = null
    }

    override fun onDestroy() {
        super.onDestroy()
        controller?.onDestroy()
        controller = null
        view = null
    }
}
