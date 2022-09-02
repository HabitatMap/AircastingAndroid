package pl.llp.aircasting.ui.view.screens.new_session.session_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import javax.inject.Inject

class SessionDetailsFragment : Fragment() {
    private var controller: SessionDetailsController? = null
    protected var view: SessionDetailsViewMvc? = null
    lateinit var listener: SessionDetailsViewMvc.Listener
    lateinit var deviceItem: DeviceItem
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

        view = SessionDetailsViewFactory.get(inflater, container, childFragmentManager, deviceItem, sessionUUID, sessionType)
        controller = sessionDetailsControllerFactory.get(activity, view, sessionType, childFragmentManager)
        controller?.onCreate()

        return view?.rootView
    }

    override fun onStart() {
        super.onStart()
        listener.let { controller?.registerListener(it) }
    }

    override fun onStop() {
        super.onStop()
        listener.let { controller?.unregisterListener(it) }
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
