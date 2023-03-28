package pl.llp.aircasting.ui.view.screens.new_session.confirmation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BaseFragment
import javax.inject.Inject

class ConfirmationFragment : BaseFragment<ConfirmationViewMvcImpl, ConfirmationController>() {
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
            .userDependentComponent.inject(this)

        view = ConfirmationViewFactory.get(inflater, container, childFragmentManager, session, settings.areMapsDisabled())
        controller = ConfirmationController(view, settings)
        return view?.rootView
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
