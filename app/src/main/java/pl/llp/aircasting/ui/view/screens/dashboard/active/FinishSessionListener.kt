package pl.llp.aircasting.ui.view.screens.dashboard.active

import pl.llp.aircasting.data.model.LocalSession

interface FinishSessionListener {
    fun onFinishSessionConfirmed(localSession: LocalSession)
    fun onFinishAndSyncSessionConfirmed(localSession: LocalSession)
}
