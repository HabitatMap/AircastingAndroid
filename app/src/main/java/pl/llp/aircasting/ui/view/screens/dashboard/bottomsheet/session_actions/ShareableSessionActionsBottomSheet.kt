package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.session_actions

import kotlinx.android.synthetic.main.session_actions_modifiable.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BottomSheet

abstract class ShareableSessionActionsBottomSheet(private val mListener: Listener?) : BottomSheet() {
    constructor() : this(null)
    interface Listener : SessionActionsBottomSheetListener {
        fun shareSessionPressed()
    }
    override fun layoutId(): Int {
        return R.layout.session_actions_shareable
    }

    override fun setup() {
        val shareButton = contentView?.share_session_button
        shareButton?.setOnClickListener {
            mListener?.shareSessionPressed()
        }

        val cancelButton = contentView?.cancel_button
        cancelButton?.setOnClickListener {
            dismiss()
        }
    }
}