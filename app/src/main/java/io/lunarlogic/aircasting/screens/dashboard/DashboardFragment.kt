package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.screens.common.BaseFragment
import io.lunarlogic.aircasting.screens.sync.synced.AirbeamSyncedController
import io.lunarlogic.aircasting.screens.sync.synced.AirbeamSyncedViewMvcImpl

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
