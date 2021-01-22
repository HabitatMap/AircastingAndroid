package io.lunarlogic.aircasting.screens.new_session.confirmation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.models.Session
import javax.inject.Inject

class ConfirmationFragment() : Fragment() {
    private var controller: ConfirmationController? = null
    lateinit var listener: ConfirmationViewMvc.Listener
    lateinit var session: Session

    @Inject
    lateinit var settings: Settings

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)


        val view = ConfirmationViewFactory.get(inflater, container, childFragmentManager, session)
        controller = ConfirmationController(context, view, settings)
        return view.rootView
    }

    override fun onStart() {
        super.onStart()

        controller?.registerToEventBus()
        controller?.registerListener(listener)
        controller?.onStart(context)
    }

    override fun onStop() {
        super.onStop()
        controller?.unregisterListener(listener)
    }
}
