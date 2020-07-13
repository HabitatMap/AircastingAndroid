package io.lunarlogic.aircasting.screens.new_session.select_device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class SelectDeviceTypeFragment() : Fragment() {
    private var controller: SelectDeviceTypeController? = null
    var listener: SelectDeviceTypeViewMvc.Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = SelectDeviceTypeViewMvcImpl(inflater, container)
        controller = SelectDeviceTypeController(context, view)

        return view.rootView
    }

    override fun onStart() {
        super.onStart()
        listener?.let { controller?.registerListener(it) }

    }

    override fun onStop() {
        super.onStop()
        listener?.let { controller?.unregisterListener(it) }
    }
}