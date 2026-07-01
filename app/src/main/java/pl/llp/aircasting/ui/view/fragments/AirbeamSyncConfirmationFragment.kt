package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.screens.sync.confirmation.AirbeamSyncConfirmationController
import pl.llp.aircasting.ui.view.screens.sync.confirmation.AirbeamSyncConfirmationViewMvc
import pl.llp.aircasting.ui.view.screens.sync.confirmation.AirbeamSyncConfirmationViewMvcImpl

import pl.llp.aircasting.ui.view.common.BaseWizardNavigator

class AirbeamSyncConfirmationFragment(
    private val estimatedSeconds: Long
) : BaseFragment<AirbeamSyncConfirmationViewMvcImpl, AirbeamSyncConfirmationController>(), BaseWizardNavigator.BackPressedListener {
    lateinit var listener: AirbeamSyncConfirmationViewMvc.Listener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .userDependentComponent?.inject(this)

        view = AirbeamSyncConfirmationViewMvcImpl(layoutInflater, null, estimatedSeconds)
        controller = AirbeamSyncConfirmationController(view)

        return view?.rootView
    }

    override fun onStart() {
        super.onStart()
        if (::listener.isInitialized) {
            controller?.registerListener(listener)
        }
    }

    override fun onStop() {
        super.onStop()
        if (::listener.isInitialized) {
            controller?.unregisterListener(listener)
        }
    }

    override fun onBackPressed() {
        if (::listener.isInitialized) {
            listener.onNoClicked()
        }
    }
}
