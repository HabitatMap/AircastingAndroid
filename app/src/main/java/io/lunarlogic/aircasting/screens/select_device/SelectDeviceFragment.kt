package io.lunarlogic.aircasting.screens.select_device


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class SelectDeviceFragment : Fragment() {
    private var mSelectDeviceController: SelectDeviceController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val selectDeviceView =
            SelectDeviceViewMvcImpl(
                layoutInflater,
                null
            )
        mSelectDeviceController =
            SelectDeviceController(
                context,
                selectDeviceView
            )

        return selectDeviceView.rootView
    }

    override fun onStart() {
        super.onStart()
        mSelectDeviceController!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mSelectDeviceController!!.onStop()
    }
}
