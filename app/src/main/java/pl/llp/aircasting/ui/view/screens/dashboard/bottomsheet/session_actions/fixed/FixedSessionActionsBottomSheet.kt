package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.session_actions.fixed

import android.widget.Button
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.session_actions.ShareableSessionActionsBottomSheet

abstract class FixedSessionActionsBottomSheet(private val mListener: Listener?) :
    ShareableSessionActionsBottomSheet(mListener) {
    constructor() : this(null)

    sealed interface Listener : ShareableSessionActionsBottomSheet.Listener {
        fun createThresholdAlertPressed()
    }

    override fun layoutId() = R.layout.session_actions_fixed_unmodifiable

    override fun setup() {
        super.setup()

        val createThresholdAlertButton =
            contentView?.findViewById<Button>(R.id.create_threshold_alert_button)
        createThresholdAlertButton?.setOnClickListener {
            mListener?.createThresholdAlertPressed()
        }
    }
}