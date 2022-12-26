package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.fixed

import android.widget.Button
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.ShareableSessionActionsBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.theshold_alerts.CreateThresholdAlertBottomSheet

abstract class FixedSessionActionsBottomSheet(private val session: Session?) :
    ShareableSessionActionsBottomSheet(session) {
    constructor() : this(null)

    override fun layoutId() = R.layout.session_actions_fixed_unmodifiable

    override fun setup() {
        super.setup()

        val createThresholdAlertButton =
            contentView?.findViewById<Button>(R.id.create_threshold_alert_button)
        createThresholdAlertButton?.setOnClickListener {
            CreateThresholdAlertBottomSheet(session).show(parentFragmentManager)
            dismiss()
        }
    }
}