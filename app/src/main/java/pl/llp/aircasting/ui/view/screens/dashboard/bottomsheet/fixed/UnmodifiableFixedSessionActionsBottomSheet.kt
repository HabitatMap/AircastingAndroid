package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.fixed

import pl.llp.aircasting.data.model.Session

class UnmodifiableFixedSessionActionsBottomSheet(session: Session?) :
    FixedSessionActionsBottomSheet(session) {
    constructor() : this(null)
}