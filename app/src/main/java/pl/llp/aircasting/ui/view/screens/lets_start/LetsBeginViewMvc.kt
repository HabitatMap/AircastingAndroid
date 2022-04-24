package pl.llp.aircasting.ui.view.screens.lets_begin

import pl.llp.aircasting.ui.view.screens.common.ObservableViewMvc

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
