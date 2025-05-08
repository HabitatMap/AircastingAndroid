package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.mobile.dormant

import android.widget.Toast
import kotlinx.android.synthetic.main.session_actions_modifiable.view.delete_session_button
import kotlinx.android.synthetic.main.session_actions_modifiable.view.edit_session_button
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.ShareableSessionActionsBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.delete.DeleteSessionBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.edit.EditSessionBottomSheet
import pl.llp.aircasting.util.extensions.isNotConnected
import pl.llp.aircasting.util.extensions.showToast

class MobileDormantSessionActionsBottomSheet(
    private val session: Session?
) : ShareableSessionActionsBottomSheet(session) {
    constructor() : this(null)

    override fun layoutId() = R.layout.session_actions_modifiable

    override fun setup() {
        super.setup()

        val editButton = contentView?.edit_session_button
        editButton?.setOnClickListener {
            if (requireActivity().isNotConnected) {
                requireActivity().showToast(
                    getString(R.string.errors_network_required_edit),
                    Toast.LENGTH_LONG
                )
            } else {
                EditSessionBottomSheet(session).show(parentFragmentManager)
                dismiss()
            }
        }

        val deleteButton = contentView?.delete_session_button
        deleteButton?.setOnClickListener {
            if (context.isNotConnected) {
                requireActivity().showToast(
                    getString(R.string.errors_network_required_delete_streams),
                    Toast.LENGTH_LONG
                )
            } else {
                DeleteSessionBottomSheet(session).show(parentFragmentManager)
                dismiss()
            }
        }
    }
}
