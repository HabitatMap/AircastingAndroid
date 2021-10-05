package pl.llp.aircasting.screens.main

import kotlinx.android.synthetic.main.reorder_sessions_bottom_sheet.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.common.BottomSheet

class ReorderSessionsBottomSheet(
    private val listener: Listener
) : BottomSheet() {
    interface Listener {
        fun onReorderSessionsClicked()
    }

    override fun layoutId(): Int {
        return R.layout.reorder_sessions_bottom_sheet
    }

    override fun setup() {
        val reorderSessionsButton = contentView?.reorder_sessions_button
        reorderSessionsButton?.setOnClickListener {
            listener.onReorderSessionsClicked()
        }

        val cancelButton = contentView?.cancel_button
        cancelButton?.setOnClickListener {
            dismiss()
        }
    }
}
