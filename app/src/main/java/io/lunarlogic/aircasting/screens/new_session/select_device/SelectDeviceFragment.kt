package io.lunarlogic.aircasting.screens.new_session.select_device

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment


class SelectDeviceFragment() : Fragment() {
    private var controller: SelectDeviceController? = null
    var listener: SelectDeviceViewMvc.Listener? = null

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            SelectDeviceViewMvcImpl(
                layoutInflater,
                null
            )

        activity?.packageManager?.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            // TODO: handle BLE not supported
        }

        if (listener != null) {
            controller =
                SelectDeviceController(
                    context,
                    view,
                    listener!!
                )
        }

        return view.rootView
    }

    override fun onStart() {
        super.onStart()
        controller?.onStart()
    }

    override fun onStop() {
        super.onStop()
        controller?.onStop()
    }
}
