package pl.llp.aircasting.ui.view.screens.common

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.R
import kotlinx.android.synthetic.main.dialog_alert_aircasting.view.*

class AircastingAlertDialog(
    mFragmentManager : FragmentManager,
    private val alertHeader : String?,
    private val alertDescription: String?
) : BaseDialog(mFragmentManager) {
    lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.dialog_alert_aircasting, null)

        mView.header.text = alertHeader
        mView.description.text = alertDescription

        mView.ok_button.setOnClickListener {
            dismiss()
        }

        return mView
    }
}
