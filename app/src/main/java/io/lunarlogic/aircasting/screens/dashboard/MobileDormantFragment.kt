package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.lib.Settings
import javax.inject.Inject


class MobileDormantFragment : Fragment() {
    private var controller: MobileDormantController? = null
    private val sessionsViewModel by activityViewModels<SessionsViewModel>()

    @Inject
    lateinit var settings: Settings

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        val view = MobileDormantViewMvcImpl(layoutInflater, null)
        controller = MobileDormantController(context, view, sessionsViewModel, viewLifecycleOwner, settings)
        controller!!.onCreate()

        return view.rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        controller!!.onDestroy()
    }
}