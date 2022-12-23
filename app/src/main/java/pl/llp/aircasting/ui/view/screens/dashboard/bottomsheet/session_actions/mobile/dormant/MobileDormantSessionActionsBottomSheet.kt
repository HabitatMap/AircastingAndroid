package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.session_actions.mobile.dormant

import kotlinx.android.synthetic.main.session_actions_modifiable.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.session_actions.ModifiableSessionActionsBottomSheetListener
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.session_actions.ShareableSessionActionsBottomSheet

class MobileDormantSessionActionsBottomSheet(
    private val mListener: Listener?
) : ShareableSessionActionsBottomSheet(mListener) {
    interface Listener :
        ShareableSessionActionsBottomSheet.Listener,
        ModifiableSessionActionsBottomSheetListener

    override fun layoutId() = R.layout.session_actions_modifiable

    override fun setup() {
        super.setup()

        val editButton = contentView?.edit_session_button
        editButton?.setOnClickListener {
            mListener?.editSessionPressed()
        }

        val deleteButton = contentView?.delete_session_button
        deleteButton?.setOnClickListener {
            mListener?.deleteSessionPressed()
        }
    }
}
