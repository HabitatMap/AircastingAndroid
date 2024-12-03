package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet

import kotlinx.android.synthetic.main.session_actions_modifiable.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.share.ShareSessionBottomSheet

abstract class ShareableSessionActionsBottomSheet(private val session: Session?) : BottomSheet() {
    constructor() : this(null)

    override fun layoutId(): Int {
        return R.layout.session_actions_shareable
    }

    override fun setup() {
        val shareButton = contentView?.share_session_button
        shareButton?.setOnClickListener {
            ShareSessionBottomSheet(session).show(parentFragmentManager)
            dismiss()
        }

        val cancelButton = contentView?.cancel_button
        cancelButton?.setOnClickListener {
            dismiss()
        }
    }
}

abstract class NonShareableSessionActionsBottomSheet() : BottomSheet() {
    override fun layoutId(): Int {
        return R.layout.session_action_fixed_unmodifiable_unshareable
    }

    override fun setup() {
        val cancelButton = contentView?.cancel_button
        cancelButton?.setOnClickListener {
            dismiss()
        }
    }
}