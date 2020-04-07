package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class DashboardFragment : Fragment() {
    private var mDashboardController: DashboardController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dashboardView = DashboardViewMvcImpl(inflater, container)
        mDashboardController = DashboardController(context, dashboardView)

        return dashboardView.rootView
    }

    override fun onStart() {
        super.onStart()
        mDashboardController!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mDashboardController!!.onStop()
    }
}