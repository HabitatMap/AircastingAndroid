package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.lunarlogic.aircasting.screens.common.BaseFragment

class DashboardFragment : BaseFragment<DashboardViewMvcImpl, DashboardController>() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = DashboardViewMvcImpl(inflater, container, childFragmentManager)
        controller = DashboardController(view)
        val tabId = arguments?.get("tabId") as Int?
        controller?.onCreate(tabId)

        return view?.rootView
    }
}
