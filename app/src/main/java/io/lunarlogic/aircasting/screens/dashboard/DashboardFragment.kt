package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels


class DashboardFragment : Fragment() {
    private var controller: DashboardController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = DashboardViewMvcImpl(inflater, container, childFragmentManager)
        controller = DashboardController(view)
        controller?.onCreate()

        return view.rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        controller?.onDestroy()
    }
}