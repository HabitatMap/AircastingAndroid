package io.lunarlogic.aircasting.screens.new_session.session_details

import io.lunarlogic.aircasting.sensor.Session


interface FixedSessionDetailsViewMvc: SessionDetailsViewMvc {
    fun registerOnStreamingMethodChangedListener(listener: OnStreamingMethodChangedListener)
    fun registerOnRefreshNetworksListener(listener: OnRefreshNetworksListener)
    fun bindNetworks(networks: List<Network>)

    interface OnStreamingMethodChangedListener {
        fun onStreamingMethodChanged(streamingMethod: Session.StreamingMethod)
    }

    interface OnRefreshNetworksListener {
        fun onRefreshClicked()
    }

    interface OnPasswordProvidedListener {
        fun onNetworkPasswordProvided(password: String)
    }
}
