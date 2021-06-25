package pl.llp.aircasting.screens.lets_start

import pl.llp.aircasting.screens.common.ObservableViewMvc

interface LetsStartViewMvc: ObservableViewMvc<LetsStartViewMvc.Listener> {
    fun showMoreInfoDialog()
    interface Listener {
        fun onFixedSessionSelected()
        fun onMobileSessionSelected()
        fun onSyncSelected()
        fun onMoreInfoClicked()
    }
}
