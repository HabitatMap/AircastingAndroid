package io.lunarlogic.aircasting.screens.lets_start

import io.lunarlogic.aircasting.screens.common.ObservableViewMvc

interface LetsStartViewMvc: ObservableViewMvc<LetsStartViewMvc.Listener> {
    fun showMoreInfoDialog()
    interface Listener {
        fun onFixedSessionSelected()
        fun onMobileSessionSelected()
        fun onSyncSelected()
        fun onClearSDCardSelected()
        fun onMoreInfoClicked()
    }
}
