package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.session_actions.fixed

import android.widget.Button
import kotlinx.android.synthetic.main.session_actions_modifiable.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.session_actions.ModifiableSessionActionsBottomSheetListener

class ModifiableFixedSessionActionsBottomSheet(private val mListener: Listener?) :
    FixedSessionActionsBottomSheet(mListener) {
    constructor() : this(null)

    interface Listener :
        ModifiableSessionActionsBottomSheetListener,
        UnmodifiableFixedSessionActionsBottomSheet.Listener

    override fun layoutId() = R.layout.session_actions_fixed_modifiable

    override fun setup() {
        super.setup()

        val createThresholdAlertButton =
            contentView?.findViewById<Button>(R.id.create_threshold_alert_button)
        createThresholdAlertButton?.setOnClickListener {
            mListener?.createThresholdAlertPressed()
        }

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