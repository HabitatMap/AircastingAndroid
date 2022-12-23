package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.session_actions.fixed

class UnmodifiableFixedSessionActionsBottomSheet(mListener: Listener?) :
    FixedSessionActionsBottomSheet(mListener) {
    constructor() : this(null)

    interface Listener : FixedSessionActionsBottomSheet.Listener
}