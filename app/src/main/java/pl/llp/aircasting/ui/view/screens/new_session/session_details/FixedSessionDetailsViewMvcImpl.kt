package pl.llp.aircasting.ui.view.screens.new_session.session_details

import android.graphics.drawable.Animatable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputLayout
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.TAGS_SEPARATOR
import pl.llp.aircasting.ui.view.common.BaseObservableViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import kotlinx.android.synthetic.main.network_list_item.view.*


class FixedSessionDetailsViewMvcImpl: BaseObservableViewMvc<SessionDetailsViewMvc.Listener>,
    FixedSessionDetailsViewMvc, FixedSessionDetailsViewMvc.OnPasswordProvidedListener {
    private val fragmentManager: FragmentManager
    private var sessionUUID: String
    private var deviceItem: DeviceItem
    private var indoor = true
    private var streamingMethod = Session.StreamingMethod.WIFI
    private var streamingMethodChangedListener: FixedSessionDetailsViewMvc.OnStreamingMethodChangedListener? = null
    private var networksHeaderView: TextView? = null
    private var refreshNetworksListButton: Button? = null
    private var networkListLoaded = false
    private var networkListLoader: ImageView? = null
    private var networksRecyclerView: RecyclerView? = null
    private val networksRecyclerViewAdapter: GroupAdapter<GroupieViewHolder>
    private var networksRefreshListener: FixedSessionDetailsViewMvc.OnRefreshNetworksListener? = null
    private var selectedNetworkItem: RecyclerViewNetworkItem? = null
    private var selectedNetworkPassword: String? = null
    private var sessionNameInputLayout: TextInputLayout? = null

    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        fragmentManager: FragmentManager,
        sessionUUID: String,
        deviceItem: DeviceItem
    ): super() {
        this.rootView = inflater.inflate(R.layout.fragment_fixed_session_details, parent, false)
        this.fragmentManager = fragmentManager
        this.sessionUUID = sessionUUID
        this.deviceItem = deviceItem

        val indoorToggle = rootView?.findViewById<MaterialButtonToggleGroup>(R.id.indoor_toggle)
        indoorToggle?.addOnButtonCheckedListener { _, checkedId, isChecked ->
            indoor = (checkedId == R.id.indoor_button && isChecked)
        }

        sessionNameInputLayout = rootView?.findViewById(R.id.session_name)
        networksHeaderView = rootView?.findViewById(R.id.networks_list_header)
        refreshNetworksListButton = rootView?.findViewById(R.id.refresh_network_list_button)
        refreshNetworksListButton?.setOnClickListener {
            onRefreshNetworksListClicked()
        }
        networkListLoader = rootView?.findViewById(R.id.networks_list_loader)
        networksRecyclerView = rootView?.findViewById(R.id.networks_list)
        networksRecyclerView?.layoutManager = LinearLayoutManager(rootView!!.context)
        networksRecyclerViewAdapter = GroupAdapter()
        networksRecyclerViewAdapter.setOnItemClickListener { item, view ->
            val networkItem = item as? RecyclerViewNetworkItem
            networkItem?.let { onNetworkItemClicked(it) }
        }
        networksRecyclerView?.adapter = networksRecyclerViewAdapter
        showNetworksList()

        val cellularButton = rootView?.findViewById<Button>(R.id.cellular_button)
        cellularButton?.setOnClickListener {
            streamingMethod = Session.StreamingMethod.CELLULAR
            hideNetworksList()

            streamingMethodChangedListener?.onStreamingMethodChanged(streamingMethod)
        }

        val wifiButton = rootView?.findViewById<Button>(R.id.wifi_button)
        wifiButton?.setOnClickListener {
            streamingMethod = Session.StreamingMethod.WIFI
            showNetworksList()

            streamingMethodChangedListener?.onStreamingMethodChanged(streamingMethod)
        }

        val continueButton = rootView?.findViewById<Button>(R.id.continue_button)
        continueButton?.setOnClickListener {
            onSessionDetailsContinueClicked()
        }
    }

    private fun showNetworksList() {
        if (networkListLoaded) {
            networksRecyclerView?.visibility = View.VISIBLE
            refreshNetworksListButton?.visibility = View.VISIBLE
            networkListLoader?.visibility = View.GONE
        } else {
            startLoading()
        }
        networksHeaderView?.visibility = View.VISIBLE
    }

    private fun startLoading() {
        networkListLoader?.visibility = View.VISIBLE
        networksRecyclerView?.visibility = View.INVISIBLE

        val animatable: Animatable? = networkListLoader?.drawable as? Animatable
        animatable?.start()
    }

    private fun onRefreshNetworksListClicked() {
        startLoading()
        networksRefreshListener?.onRefreshClicked()
    }

    private fun hideNetworksList() {
        networksHeaderView?.visibility = View.GONE
        refreshNetworksListButton?.visibility = View.GONE
        networkListLoader?.visibility = View.GONE
        networksRecyclerView?.visibility = View.GONE
    }

    inner class RecyclerViewNetworkItem(val network: Network, var selected: Boolean = false) : Item<GroupieViewHolder>() {
        override fun getLayout() = R.layout.network_list_item

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.label.text = network.name

            if (selected) {
                setSelectedNetworkItemStyles(viewHolder)
            } else {
                setDefaultNetworkItemStyles(viewHolder)
            }

            updateWifiIcon(network, viewHolder.itemView)
        }

        private fun updateWifiIcon(network: Network, view: View) {
            view.wifi_icon_0.visibility = View.INVISIBLE
            view.wifi_icon_1.visibility = View.INVISIBLE
            view.wifi_icon_2.visibility = View.INVISIBLE
            view.wifi_icon_3.visibility = View.INVISIBLE

            val wifiIcon = when (network.level) {
                1 -> view.wifi_icon_1
                2 -> view.wifi_icon_2
                3 -> view.wifi_icon_3
                else -> view.wifi_icon_0
            }
            wifiIcon.visibility = View.VISIBLE
        }

        private fun setSelectedNetworkItemStyles(viewHolder: GroupieViewHolder) {
            viewHolder.itemView.label.typeface = ResourcesCompat.getFont(context, R.font.moderat_trial_bold)
            viewHolder.itemView.background = ResourcesCompat.getDrawable(context.resources, R.drawable.selected_network_item_background, context.theme)
        }

        private fun setDefaultNetworkItemStyles(viewHolder: GroupieViewHolder) {
            viewHolder.itemView.label.typeface = ResourcesCompat.getFont(context, R.font.moderat_trial_regular)
            viewHolder.itemView.background = ResourcesCompat.getDrawable(context.resources, R.drawable.network_item_background, context.theme)
        }
    }

    override fun registerOnStreamingMethodChangedListener(listener: FixedSessionDetailsViewMvc.OnStreamingMethodChangedListener) {
        streamingMethodChangedListener = listener
    }

    override fun registerOnRefreshNetworksListener(listener: FixedSessionDetailsViewMvc.OnRefreshNetworksListener) {
        networksRefreshListener = listener
    }

    override fun bindNetworks(networks: List<Network>) {
        val networkItems = networks.map { RecyclerViewNetworkItem(it) }
        networksRecyclerViewAdapter.clear()
        networksRecyclerViewAdapter.addAll(networkItems)

        networkListLoaded = true
        if (streamingMethod == Session.StreamingMethod.WIFI) {
            showNetworksList()
        }
    }

    private fun onNetworkItemClicked(networkItem: RecyclerViewNetworkItem) {
        selectedNetworkItem?.selected = false

        selectedNetworkItem = networkItem
        selectedNetworkItem?.selected = true

        networksRecyclerViewAdapter.notifyDataSetChanged()

        NetworkPasswordDialog(networkItem.network.name, fragmentManager, this).show()
    }

    private fun onSessionDetailsContinueClicked() {
        val sessionName = getTextInputEditTextValue(R.id.session_name_input)
        val sessionTags = getSessionTags()
        val wifiName = selectedNetworkItem?.network?.name ?: ""
        val wifiPassword = selectedNetworkPassword ?: ""

        val errorMessage = validate(sessionName, wifiName, wifiPassword)

        if (errorMessage == null) {
            notifyAboutSuccess(sessionName, sessionTags, wifiName, wifiPassword)
        } else {
            notifyAboutValidationError(errorMessage)
        }
    }

    private fun notifyAboutValidationError(errorMessage: String) {
        for (listener in listeners) {
            listener.validationFailed(errorMessage)
        }
    }

    private fun notifyAboutSuccess(
        sessionName: String,
        sessionTags: ArrayList<String>,
        wifiName: String,
        wifiPassword: String
    ) {
        for (listener in listeners) {
            listener.onSessionDetailsContinueClicked(
                sessionUUID,
                deviceItem,
                Session.Type.FIXED,
                sessionName,
                sessionTags,
                indoor,
                streamingMethod,
                wifiName,
                wifiPassword
            )
        }
    }

    private fun getSessionTags(): ArrayList<String> {
        val string = getTextInputEditTextValue(R.id.session_tags_input)
        return ArrayList(string.split(TAGS_SEPARATOR))
    }

    private fun validate(sessionName: String, wifiName: String, wifiPassword: String): String? {
        if (sessionName.isEmpty()) {
            sessionNameInputLayout?.error = " "
            return getString(R.string.session_name_required)
        }

        if (streamingMethod == Session.StreamingMethod.WIFI && (wifiName.isEmpty() || wifiPassword.isEmpty())) {
            return getString(R.string.session_wifi_credentials_required)
        }

        return null
    }

    override fun onNetworkPasswordProvided(password: String) {
        selectedNetworkPassword = password
    }
}
