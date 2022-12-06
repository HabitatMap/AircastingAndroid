package pl.llp.aircasting.ui.view.screens.dashboard

import android.widget.ImageView
import kotlinx.android.synthetic.main.session_actions.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BottomSheet

open class SessionActionsBottomSheet(private val mListener: Listener?) : BottomSheet() {
    constructor() : this(null)
    interface Listener {
        fun editSessionPressed()
        fun shareSessionPressed()
        fun deleteSessionPressed()
    }

    var mLoader: ImageView? = null

    override fun layoutId(): Int {
        return R.layout.session_actions
    }

    override fun setup() {
        mLoader = view?.findViewById(R.id.loader)

        val editButton = contentView?.edit_session_button
        editButton?.setOnClickListener {
            mListener?.editSessionPressed()
        }

        val shareButton = contentView?.share_session_button
        shareButton?.setOnClickListener {
            mListener?.shareSessionPressed()
        }

        val deleteButton = contentView?.delete_session_button
        deleteButton?.setOnClickListener {
            mListener?.deleteSessionPressed()
        }

        val cancelButton = contentView?.cancel_button
        cancelButton?.setOnClickListener {
            dismiss()
        }
    }
}
