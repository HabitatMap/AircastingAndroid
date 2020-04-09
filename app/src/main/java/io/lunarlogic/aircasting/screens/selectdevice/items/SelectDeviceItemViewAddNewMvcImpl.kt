package io.lunarlogic.aircasting.screens.selectdevice.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.screens.selectdevice.SelectDeviceViewMvc

class SelectDeviceItemViewAddNewMvcImpl : BaseObservableViewMvc<SelectDeviceViewMvc.Listener>,
    SelectDeviceItemViewMvc {
    private var mDeviceItem: DeviceItem? = null

    constructor(inflater: LayoutInflater, parent: ViewGroup) {
        this.rootView = inflater.inflate(R.layout.select_device_item_new_device, parent, false)

        rootView?.setOnClickListener(View.OnClickListener {
            for (listener in listeners) {
                listener.onDeviceItemSelected(mDeviceItem!!)
            }
        })
    }

    override fun bindDeviceItem(deviceItem: DeviceItem) {
        mDeviceItem = deviceItem
    }
}