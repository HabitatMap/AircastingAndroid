package io.lunarlogic.aircasting.screens.new_session.select_device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class SelectDeviceTypeFragment() : Fragment() {
    private var controller: SelectDeviceTypeController? = null
    var listener: SelectDeviceTypeViewMvc.Listener? = null
    private var view: SelectDeviceTypeViewMvcImpl? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = SelectDeviceTypeViewMvcImpl(inflater, container)
        controller = SelectDeviceTypeController(view)

        return view?.rootView
    }

    override fun onStart() {
        super.onStart()
        listener?.let { controller?.registerListener(it) }

    }

    override fun onStop() {
        super.onStop()
        listener?.let { controller?.unregisterListener(it) }
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
