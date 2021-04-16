package io.lunarlogic.aircasting.screens.sync.refreshed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.screens.common.BaseFragment
import io.lunarlogic.aircasting.screens.common.BaseWizardNavigator

class RefreshedSessionsFragment(private val mFragmentManager: FragmentManager): BaseFragment<RefreshedSessionsViewMvcImpl, RefreshedSessionsController>(), BaseWizardNavigator.BackPressedListener {
    lateinit var listener: RefreshedSessionsViewMvc.Listener

    var success: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = RefreshedSessionsViewMvcImpl(layoutInflater, null, success)
        controller = RefreshedSessionsController(mFragmentManager, view)

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
