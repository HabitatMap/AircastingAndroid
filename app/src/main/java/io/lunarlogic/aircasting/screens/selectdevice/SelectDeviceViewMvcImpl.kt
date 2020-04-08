package io.lunarlogic.aircasting.screens.selectdevice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.devices.Device
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc

class SelectDeviceViewMvcImpl : BaseObservableViewMvc<SelectDeviceViewMvc.Listener>,
    SelectDeviceViewMvc,
    SelectDeviceRecyclerAdapter.Listener {

    private var mRecyclerDevices: RecyclerView? = null
    private val mAdapter: SelectDeviceRecyclerAdapter

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.activity_select_device, parent, false)

        mRecyclerDevices = findViewById(R.id.recycler_devices)
        mRecyclerDevices?.setLayoutManager(LinearLayoutManager(rootView!!.context))
        mAdapter = SelectDeviceRecyclerAdapter(
            inflater,
            this
        )
        mRecyclerDevices?.setAdapter(mAdapter)
    }

    override fun bindDevices(devices: List<Device>) {
        mAdapter.bindDevices(devices)
    }

    override fun onDeviceSelected(device: Device) {
        for (listener in listeners) {
            listener.onDeviceSelected(device)
        }
    }
}