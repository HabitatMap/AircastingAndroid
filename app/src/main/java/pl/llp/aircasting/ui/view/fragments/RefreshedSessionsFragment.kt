package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.common.BaseWizardNavigator
import pl.llp.aircasting.ui.view.screens.sync.refreshed.RefreshedSessionsController
import pl.llp.aircasting.ui.view.screens.sync.refreshed.RefreshedSessionsViewMvc
import pl.llp.aircasting.ui.view.screens.sync.refreshed.RefreshedSessionsViewMvcImpl

class RefreshedSessionsFragment :
    BaseFragment<RefreshedSessionsViewMvcImpl, RefreshedSessionsController>(),
    BaseWizardNavigator.BackPressedListener {
    lateinit var listener: RefreshedSessionsViewMvc.Listener

    var success: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = RefreshedSessionsViewMvcImpl(layoutInflater, null, success)
        controller = RefreshedSessionsController(parentFragmentManager, view)

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

    override fun onBackPressed() {
        controller?.onBackPressed()
    }
}
