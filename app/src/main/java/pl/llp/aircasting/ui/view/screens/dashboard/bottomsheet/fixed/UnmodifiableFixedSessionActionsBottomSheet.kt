package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.fixed

import android.widget.Button
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.NonShareableSessionActionsBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.theshold_alerts.CreateThresholdAlertBottomSheet

class UnmodifiableFixedSessionActionsBottomSheet(private val session: Session?) :
    NonShareableSessionActionsBottomSheet() {
    constructor() : this(null)

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