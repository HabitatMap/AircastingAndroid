package io.lunarlogic.aircasting.screens.sync.refreshed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.screens.common.BaseWizardNavigator

class RefreshedSessionsFragment(private val mFragmentManager: FragmentManager): Fragment(), BaseWizardNavigator.BackPressedListener {
    private var controller: RefreshedSessionsController? = null
    private var view: RefreshedSessionsViewMvcImpl? = null
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
