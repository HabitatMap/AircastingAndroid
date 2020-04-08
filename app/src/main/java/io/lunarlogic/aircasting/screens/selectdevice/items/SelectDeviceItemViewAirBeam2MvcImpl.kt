package io.lunarlogic.aircasting.screens.selectdevice.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.screens.selectdevice.SelectDeviceViewMvc

class SelectDeviceItemViewAirBeam2MvcImpl : BaseObservableViewMvc<SelectDeviceViewMvc.Listener>,
    SelectDeviceItemViewMvc {
    private var mTxtName: TextView
    private var mTxtId: TextView

    private var mDeviceItem: DeviceItem? = null

    constructor(inflater: LayoutInflater, parent: ViewGroup) {
        this.rootView = inflater.inflate(R.layout.select_device_item_airbeam2, parent, false)

        mTxtName = findViewById(R.id.device_name)
        mTxtId = findViewById(R.id.device_id)
        rootView?.setOnClickListener(View.OnClickListener {
            for (listener in listeners) {
                listener.onDeviceItemSelected(mDeviceItem!!)
            }
        })
    }

    override fun bindDeviceItem(deviceItem: DeviceItem) {
        mDeviceItem = deviceItem
        mTxtName.setText(deviceItem.title)
        mTxtId.setText(deviceItem.id)
    }
}