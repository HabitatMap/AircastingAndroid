package io.lunarlogic.aircasting.screens.new_session.select_device

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.*
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseObservableViewMvc
import kotlinx.android.synthetic.main.select_device_item.view.*

class SelectDeviceViewMvcImpl: BaseObservableViewMvc<SelectDeviceViewMvc.Listener>,
    SelectDeviceViewMvc {

    private var mRecyclerDevices: RecyclerView? = null
    private val mAdapter: GroupAdapter<GroupieViewHolder>
    private var mAirBeamDevicesSection: RecyclerViewSection? = null
    private var mOtherDevicesSection: RecyclerViewSection? = null
    private var mSelectedDeviceItem: DeviceItem? = null
    private var mConnectButton: Button? = null

    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.fragment_select_device, parent, false)

        mRecyclerDevices = findViewById(R.id.recycler_devices)
        mRecyclerDevices?.setLayoutManager(LinearLayoutManager(rootView!!.context))
        mAdapter = GroupAdapter()
        mAdapter.setOnItemClickListener { item, view ->
            val recyclerViewItem = item as? RecyclerViewDeviceItem
            recyclerViewItem?.let { onDeviceItemSelected(it) }
        }
        mRecyclerDevices?.setAdapter(mAdapter)
        mConnectButton = findViewById(R.id.connect_button)
        mConnectButton?.setOnClickListener {
            onConnectClicked()
        }
    }

    class RecyclerViewDeviceItem(val deviceItem: DeviceItem, var selected: Boolean = false) : Item<GroupieViewHolder>() {
        override fun getLayout() = R.layout.select_device_item

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            if (selected) {
                viewHolder.itemView.radio.setImageResource(R.drawable.ic_radio_selected)
            } else {
                viewHolder.itemView.radio.setImageResource(R.drawable.ic_radio)
            }
            viewHolder.itemView.label.text = "${deviceItem.displayName()} ${deviceItem.address}"
        }
    }

    class RecyclerViewGroupHeader(private val label: String?): Item<GroupieViewHolder>() {
        override fun getLayout() = R.layout.select_device_group_header

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.label.text = label
        }
    }

    class RecyclerViewSection(private val label: String?, private val deviceItems: List<DeviceItem>): Section() {
        private var mDeviceItemsAddresses: MutableSet<String>

        init {
            setHeader(RecyclerViewGroupHeader(label))

            mDeviceItemsAddresses = deviceItems.map { it.address }.toMutableSet()
            val recyclerItems = deviceItems.map { deviceItem -> RecyclerViewDeviceItem(deviceItem) }
            addAll(recyclerItems)
        }

        fun addUnique(deviceItem: DeviceItem) {
            if (!mDeviceItemsAddresses.contains(deviceItem.address)) {
                mDeviceItemsAddresses.add(deviceItem.address)
                val item = RecyclerViewDeviceItem(deviceItem)
                add(item)
            }
        }
    }

    override fun bindDeviceItems(deviceItems: List<DeviceItem>) {
        val airbeams = deviceItems.filter { it.isAirBeam() }
        val others = deviceItems.filter { !it.isAirBeam() }

        mAirBeamDevicesSection = RecyclerViewSection(getLabel(R.string.select_device_airbeams_label), airbeams)
        mAdapter.add(mAirBeamDevicesSection!!)
        mOtherDevicesSection = RecyclerViewSection(getLabel(R.string.select_device_others_label), others)
        mAdapter.add(mOtherDevicesSection!!)
    }

    override fun addDeviceItem(deviceItem: DeviceItem) {
        if (deviceItem.isAirBeam()) {
            mAirBeamDevicesSection?.addUnique(deviceItem)
        } else {
            mOtherDevicesSection?.addUnique(deviceItem)
        }
    }

    private fun getLabel(labelId: Int): String? {
        return this.rootView?.context?.getString(labelId)
    }

    private fun onDeviceItemSelected(recyclerViewDeviceItem: RecyclerViewDeviceItem) {
        mSelectedDeviceItem = recyclerViewDeviceItem.deviceItem

        mAirBeamDevicesSection?.let { clearSelections(it) }
        mOtherDevicesSection?.let { clearSelections(it) }

        recyclerViewDeviceItem.selected = true
        mAdapter.notifyDataSetChanged()
    }

    private fun clearSelections(section: RecyclerViewSection) {
        section.groups?.forEach { item ->
            val recyclerViewDeviceItem = item as? RecyclerViewDeviceItem
            recyclerViewDeviceItem?.selected = false
        }
    }

    private fun onConnectClicked() {
        mSelectedDeviceItem ?: return

        for (listener in listeners) {
            listener.onConnectClicked(mSelectedDeviceItem!!)
        }
    }
}
