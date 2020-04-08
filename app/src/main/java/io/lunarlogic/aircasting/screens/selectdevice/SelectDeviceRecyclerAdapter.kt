package io.lunarlogic.aircasting.screens.selectdevice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.lunarlogic.aircasting.devices.Device

class SelectDeviceRecyclerAdapter(private val mInflater: LayoutInflater, private val mListener: Listener): RecyclerView.Adapter<SelectDeviceRecyclerAdapter.MyViewHolder>(),
    SelectDeviceViewMvc.Listener {
    interface Listener {
        fun onDeviceSelected(device: Device)
    }

    class MyViewHolder(private val mViewMvc: SelectDeviceItemViewMvc) :
        RecyclerView.ViewHolder(mViewMvc.rootView!!) {
        val view: SelectDeviceItemViewMvc get() = mViewMvc
    }

    private var mDevices: List<Device> = java.util.ArrayList()

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.view.bindDevice(mDevices.get(position))
    }

    override fun getItemCount(): Int {
        return mDevices.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewMvc = SelectDeviceItemViewMvcImpl(
            mInflater,
            parent
        )
        viewMvc.registerListener(this)
        return MyViewHolder(
            viewMvc
        )
    }

    fun bindDevices(devices: List<Device>) {
        mDevices = ArrayList<Device>(devices)
        notifyDataSetChanged()
    }

    override fun onDeviceSelected(device: Device) {
        mListener.onDeviceSelected(device)
    }
}