package pl.llp.aircasting.screens.lets_begin

import pl.llp.aircasting.screens.common.ObservableViewMvc

interface LetsBeginViewMvc: ObservableViewMvc<LetsBeginViewMvc.Listener> {
    fun showMoreInfoDialog()
    interface Listener {
        fun onFixedSessionSelected()
        fun onMobileSessionSelected()
        fun onSyncSelected()
        fun onMoreInfoClicked()
        fun onFollowSessionSelected()
    }
}
