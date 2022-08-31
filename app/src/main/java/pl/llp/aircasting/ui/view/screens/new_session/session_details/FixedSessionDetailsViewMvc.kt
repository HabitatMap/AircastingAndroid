package pl.llp.aircasting.ui.view.screens.new_session.session_details

import pl.llp.aircasting.data.model.Session


interface FixedSessionDetailsViewMvc: SessionDetailsViewMvc {
    fun registerOnStreamingMethodChangedListener(listener: OnStreamingMethodChangedListener)
    fun registerOnRefreshNetworksListener(listener: OnRefreshNetworksListener)
    fun registerOnNetworkDialogStateChangedListener(listener: OnNetworkDialogStateChangedListener)

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

    interface OnNetworkDialogStateChangedListener {
        fun onDialogIsAboutToBeShown()
        fun onDialogIsAboutToBeClosed()
    }
}
