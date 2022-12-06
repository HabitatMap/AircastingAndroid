package pl.llp.aircasting.ui.view.screens.dashboard

import android.widget.Button
import pl.llp.aircasting.R

class FixedSessionActionsBottomSheet(
    private val mListener: Listener?
) : SessionActionsBottomSheet(mListener) {
    constructor() : this(null)

    interface Listener : SessionActionsBottomSheet.Listener {
        fun createThresholdAlertPressed()
    }

    override fun layoutId() = R.layout.session_actions_fixed

    override fun setup() {
        super.setup()

        val createThresholdAlertButton =
            contentView?.findViewById<Button>(R.id.create_threshold_alert_button)
        createThresholdAlertButton?.setOnClickListener {
            mListener?.createThresholdAlertPressed()
        }
    }
}