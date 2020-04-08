package io.lunarlogic.aircasting.screens.selectdevice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.devices.Device
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class SelectDeviceItemViewMvcImpl : BaseObservableViewMvc<SelectDeviceViewMvc.Listener>,
    SelectDeviceItemViewMvc {
    private var mTxtTitle: TextView

    private var mDevice: Device? = null

    constructor(inflater: LayoutInflater, parent: ViewGroup) {
        this.rootView = inflater.inflate(R.layout.select_device_item, parent, false)

        mTxtTitle = findViewById(R.id.device_name)
        rootView?.setOnClickListener(View.OnClickListener {
            for (listener in listeners) {
                listener.onDeviceSelected(mDevice!!)
            }
        })
    }

    override fun bindDevice(device: Device) {
        mDevice = device
        mTxtTitle.setText(device.name)
    }
}