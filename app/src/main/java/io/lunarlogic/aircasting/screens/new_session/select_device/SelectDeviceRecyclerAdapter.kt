package io.lunarlogic.aircasting.screens.new_session.select_device

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.lunarlogic.aircasting.screens.new_session.select_device.items.*

class SelectDeviceRecyclerAdapter(private val mInflater: LayoutInflater, private val mListener: Listener): RecyclerView.Adapter<SelectDeviceRecyclerAdapter.MyViewHolder>(),
    SelectDeviceViewMvc.Listener {
    interface Listener {
        fun onDeviceItemSelected(deviceItem: DeviceItem)
    }

    class MyViewHolder(private val mViewMvc: SelectDeviceItemViewMvc) :
        RecyclerView.ViewHolder(mViewMvc.rootView!!) {
        val view: SelectDeviceItemViewMvc get() = mViewMvc
    }

    private var mDeviceItems: MutableList<DeviceItem> = mutableListOf()

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.view.bindDeviceItem(mDeviceItems.get(position))
    }

    override fun getItemCount(): Int {
        return mDeviceItems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc = SelectDeviceItemViewMvcImpl(mInflater, parent)
        viewMvc!!.registerListener(this)
        return MyViewHolder(
            viewMvc!!
        )
    }

    fun bindDeviceItems(deviceItems: List<DeviceItem>) {
        mDeviceItems.addAll(deviceItems)
        notifyDataSetChanged()
    }

    fun addDeviceItem(deviceItem: DeviceItem) {
        val existingDevice = mDeviceItems.find { it.id == deviceItem.id }
        if (existingDevice == null) {
            mDeviceItems.add(deviceItem)
            notifyDataSetChanged()
        }
    }

    override fun onDeviceItemSelected(deviceItem: DeviceItem) {
        mListener.onDeviceItemSelected(deviceItem)
    }
}