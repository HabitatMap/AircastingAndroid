package io.lunarlogic.aircasting.screens.new_session.select_device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.lunarlogic.aircasting.screens.common.BaseFragment
import io.lunarlogic.aircasting.screens.new_session.choose_location.ChooseLocationController
import io.lunarlogic.aircasting.screens.new_session.choose_location.ChooseLocationViewMvcImpl

class SelectDeviceTypeFragment() :  BaseFragment<SelectDeviceTypeViewMvcImpl, SelectDeviceTypeController>()  {
    var listener: SelectDeviceTypeViewMvc.Listener? = null

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
}
