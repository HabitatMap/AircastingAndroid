package io.lunarlogic.aircasting.screens.selectdevice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.devices.Device
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.screens.selectdevice.items.DeviceItem

class SelectDeviceViewMvcImpl : BaseObservableViewMvc<SelectDeviceViewMvc.Listener>,
    SelectDeviceViewMvc,
    SelectDeviceRecyclerAdapter.Listener {

    private var mRecyclerDevices: RecyclerView? = null
    private val mAdapter: SelectDeviceRecyclerAdapter
    val columnsSpan = 2

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.activity_select_device, parent, false)

        mRecyclerDevices = findViewById(R.id.recycler_devices)
        mRecyclerDevices?.setLayoutManager(GridLayoutManager(rootView!!.context, columnsSpan))
        mAdapter = SelectDeviceRecyclerAdapter(
            inflater,
            this
        )
        mRecyclerDevices?.setAdapter(mAdapter)
    }

    override fun bindDevices(devices: List<Device>) {
        mAdapter.bindDevices(devices)
    }

    override fun onDeviceItemSelected(deviceItem: DeviceItem) {
        for (listener in listeners) {
            listener.onDeviceItemSelected(deviceItem)
        }
    }
}