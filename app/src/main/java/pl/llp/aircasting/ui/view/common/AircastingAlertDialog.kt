package pl.llp.aircasting.ui.view.common

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.dialog_alert_aircasting.view.*
import pl.llp.aircasting.R

class AircastingAlertDialog(
    mFragmentManager : FragmentManager,
    private val alertHeader : String?,
    private val alertDescription: String?,
    private val onOkClicked: (() -> Unit)? = null,
) : BaseDialog(mFragmentManager) {
    lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.dialog_alert_aircasting, null)

        mView.header.text = alertHeader
        mView.description.text = alertDescription

        isCancelable = false

        mView.ok_button.setOnClickListener {
            dismiss()
            onOkClicked?.invoke()
        }

        return mView
    }
}
