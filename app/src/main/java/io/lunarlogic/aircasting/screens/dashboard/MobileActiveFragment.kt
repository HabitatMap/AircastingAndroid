package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels


class MobileActiveFragment : Fragment() {
    private var controller: MobileActiveController? = null
    private val sessionsViewModel by activityViewModels<SessionsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = MobileActiveViewMvcImpl(layoutInflater, null)
        controller = MobileActiveController(context, view, sessionsViewModel, viewLifecycleOwner)
        controller!!.onCreate()

        return view.rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        controller!!.onDestroy()
    }
}