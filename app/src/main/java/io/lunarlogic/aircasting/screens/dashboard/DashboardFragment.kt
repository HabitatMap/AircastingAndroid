package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels


class DashboardFragment : Fragment() {
    private val sessionsViewModel by activityViewModels<SessionsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = DashboardViewMvcImpl(inflater, container, childFragmentManager)
        return view.rootView
    }
}