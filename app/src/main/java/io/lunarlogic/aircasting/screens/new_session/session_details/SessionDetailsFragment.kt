package io.lunarlogic.aircasting.screens.new_session.session_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.sensor.Session
import io.lunarlogic.aircasting.sensor.SessionBuilder
import javax.inject.Inject

class SessionDetailsFragment() : Fragment() {
    private var controller: SessionDetailsController? = null
    lateinit var listener: SessionDetailsViewMvc.Listener
    lateinit var deviceId: String
    lateinit var sessionUUID: String
    lateinit var sessionType: Session.Type

    @Inject
    lateinit var sessionDetailsControllerFactory: SessionDetailsControllerFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        val view = SessionDetailsViewFactory.get(inflater, container, childFragmentManager, deviceId, sessionUUID, sessionType)
        controller = sessionDetailsControllerFactory.get(activity, view, sessionType, childFragmentManager)
        controller?.onCreate()

        return view.rootView
    }

    override fun onStart() {
        super.onStart()
        listener.let { controller?.registerListener(it) }
    }

    override fun onStop() {
        super.onStop()
        listener.let { controller?.unregisterListener(it) }
    }
}
