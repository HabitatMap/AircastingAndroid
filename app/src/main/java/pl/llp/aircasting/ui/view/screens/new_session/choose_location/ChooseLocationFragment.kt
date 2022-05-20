package pl.llp.aircasting.ui.view.screens.new_session.choose_location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BaseFragment

class ChooseLocationFragment() : BaseFragment<ChooseLocationViewMvcImpl, ChooseLocationController>() {
    lateinit var listener: ChooseLocationViewMvc.Listener
    lateinit var session: Session
    lateinit var errorHandler: ErrorHandler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = ChooseLocationViewMvcImpl(inflater, container, childFragmentManager, session, errorHandler)
        controller = ChooseLocationController(view)

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
