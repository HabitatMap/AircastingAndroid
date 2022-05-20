package pl.llp.aircasting.ui.view.common

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.battery_dialog_alert_aircasting.view.*
import kotlinx.android.synthetic.main.dialog_alert_aircasting.view.description
import kotlinx.android.synthetic.main.dialog_alert_aircasting.view.header
import kotlinx.android.synthetic.main.dialog_alert_aircasting.view.ok_button
import pl.llp.aircasting.R

class BatteryAlertDialog(
    mFragmentManager: FragmentManager,
    private val alertHeader: String?,
    private val alertDescription: String?
) : BaseDialog(mFragmentManager) {
    lateinit var mView: View

    @RequiresApi(Build.VERSION_CODES.M)
    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.battery_dialog_alert_aircasting, null)

        mView.header.text = alertHeader
        mView.description.text = alertDescription

        mView.ok_button.setOnClickListener {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            startActivity(intent)
            dismiss()
        }
        mView.cancel_btn.setOnClickListener {
            dismiss()
        }

        return mView
    }
}
