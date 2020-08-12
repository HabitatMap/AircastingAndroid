package io.lunarlogic.aircasting.screens.new_session.select_device

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import io.lunarlogic.aircasting.screens.new_session.select_device.items.DeviceItem

class SelectDeviceViewMvcImpl : BaseObservableViewMvc<SelectDeviceViewMvc.Listener>,
    SelectDeviceViewMvc,
    SelectDeviceRecyclerAdapter.Listener {

    private var mRecyclerDevices: RecyclerView? = null
    private val mAdapter: SelectDeviceRecyclerAdapter

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.fragment_select_device, parent, false)

        mRecyclerDevices = findViewById(R.id.recycler_devices)
        mRecyclerDevices?.setLayoutManager(LinearLayoutManager(rootView!!.context))
        mAdapter = SelectDeviceRecyclerAdapter(
            inflater,
            this
        )
        mRecyclerDevices?.setAdapter(mAdapter)
    }

    override fun bindDeviceItems(deviceItems: List<DeviceItem>) {
        mAdapter.bindDeviceItems(deviceItems)
    }

    override fun addDeviceItem(deviceItem: DeviceItem) {
        mAdapter.addDeviceItem(deviceItem)
    }

    override fun onDeviceItemSelected(deviceItem: DeviceItem) {
        for (listener in listeners) {
            listener.onDeviceItemSelected(deviceItem)
        }
    }
}
