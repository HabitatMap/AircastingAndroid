package io.lunarlogic.aircasting.screens.new_session.session_details


interface FixedSessionDetailsViewMvc: SessionDetailsViewMvc {
    fun registerOnRefreshNetworksListener(refreshListener: OnRefreshNetworksListener)
    fun bindNetworks(networks: List<Network>)

    interface OnRefreshNetworksListener {
        fun onRefreshClicked()
    }

    interface OnPasswordProvidedListener {
        fun onNetworkPasswordProvided(password: String)
    }
}
