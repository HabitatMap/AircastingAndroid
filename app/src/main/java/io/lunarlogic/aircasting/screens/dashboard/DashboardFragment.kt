package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class DashboardFragment : Fragment() {
    private var controller: DashboardController? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = DashboardViewMvcImpl(inflater, container, childFragmentManager)
        controller = DashboardController(view)
        val tabId = arguments?.get("tabId") as Int?
        controller?.onCreate(tabId)

        return view.rootView
    }
}
