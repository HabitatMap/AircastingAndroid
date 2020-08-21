package io.lunarlogic.aircasting.screens.new_session.session_details


interface FixedSessionDetailsViewMvc: SessionDetailsViewMvc {
    interface OnRefreshNetworksListener {
        fun onRefreshClicked()
    }

    fun registerOnRefreshNetworksListener(refreshListener: OnRefreshNetworksListener)
    fun bindNetworks(networks: List<Network>)
}
