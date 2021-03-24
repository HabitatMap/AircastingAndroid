package io.lunarlogic.aircasting.screens.new_session.choose_location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.models.Session

class ChooseLocationFragment() : Fragment() {
    private var controller: ChooseLocationController? = null
    lateinit var listener: ChooseLocationViewMvc.Listener
    lateinit var session: Session
    lateinit var errorHandler: ErrorHandler
    private var view: ChooseLocationViewMvcImpl? = null

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

    override fun onDestroy() {
        super.onDestroy()
        view = null
        controller?.onDestroy()
        controller = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view = null
        controller?.onDestroy()
        controller = null
    }

}
