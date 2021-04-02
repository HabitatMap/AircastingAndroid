package io.lunarlogic.aircasting.screens.new_session.choose_location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.common.BaseFragment
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.AirBeamConnectedController
import io.lunarlogic.aircasting.screens.new_session.connect_airbeam.AirBeamConnectedViewMvcImpl

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
