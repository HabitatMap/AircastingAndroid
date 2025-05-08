package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.fixed

import android.widget.Toast
import kotlinx.android.synthetic.main.session_actions_modifiable.view.delete_session_button
import kotlinx.android.synthetic.main.session_actions_modifiable.view.edit_session_button
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.delete.DeleteSessionBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.edit.EditSessionBottomSheet
import pl.llp.aircasting.util.extensions.isNotConnected
import pl.llp.aircasting.util.extensions.showToast

class ModifiableFixedSessionActionsBottomSheet(
    private val session: Session?
    ) : FixedSessionActionsBottomSheet(session) {
    constructor() : this(null)

    override fun layoutId() = R.layout.session_actions_fixed_modifiable

    override fun setup() {
        super.setup()

        val editButton = contentView?.edit_session_button
        editButton?.setOnClickListener {
            EditSessionBottomSheet(session)
                .show(parentFragmentManager)
            dismiss()
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