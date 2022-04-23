package pl.llp.aircasting.screens.lets_begin

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
