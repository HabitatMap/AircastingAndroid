package io.lunarlogic.aircasting.screens.selectdevice

import java.util.Collections
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.lunarlogic.aircasting.devices.Device
import io.lunarlogic.aircasting.screens.selectdevice.items.*

class SelectDeviceRecyclerAdapter(private val mInflater: LayoutInflater, private val mListener: Listener): RecyclerView.Adapter<SelectDeviceRecyclerAdapter.MyViewHolder>(),
    SelectDeviceViewMvc.Listener {
    interface Listener {
        fun onDeviceItemSelected(deviceItem: DeviceItem)
    }

    class MyViewHolder(private val mViewMvc: SelectDeviceItemViewMvc) :
        RecyclerView.ViewHolder(mViewMvc.rootView!!) {
        val view: SelectDeviceItemViewMvc get() = mViewMvc
    }

    private var mDeviceItems: MutableList<DeviceItem> = mutableListOf(AddNewDeviceItem())
    private val mDeviceItemViewFactory = SelectDeviceItemViewFactory()

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.view.bindDeviceItem(mDeviceItems.get(position))
    }

    override fun getItemCount(): Int {
        return mDeviceItems.size
    }

    override fun getItemViewType(position: Int): Int {
        val deviceItem = mDeviceItems.get(position)
        return deviceItem.viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        System.out.println("ANIA viewType: " + viewType)
        val viewMvc =
            mDeviceItemViewFactory.get(
                viewType,
                mInflater,
                parent
            )
        viewMvc!!.registerListener(this)
        return MyViewHolder(
            viewMvc!!
        )
    }

    fun bindDevices(devices: List<Device>) {
        mDeviceItems.addAll(devices.map {
            AirBeam2DeviceItem(it)
        })
        notifyDataSetChanged()
    }

    override fun onDeviceItemSelected(deviceItem: DeviceItem) {
        mListener.onDeviceItemSelected(deviceItem)
    }
}