package io.lunarlogic.aircasting.screens.map

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputEditText
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseDialog
import io.lunarlogic.aircasting.sensor.SensorThreshold
import kotlinx.android.synthetic.main.hlu_dialog.view.*

class HLUDialog(
    private var mSensorThreshold: SensorThreshold?,
    mFragmentManager: FragmentManager,
    private val listener: MapViewMvc.HLUDialogListener
): BaseDialog(mFragmentManager) {
    private lateinit var mView: View

    override fun setupView(inflater: LayoutInflater): View {
        mView = inflater.inflate(R.layout.hlu_dialog, null)

        setupView()

        mView.save_button.setOnClickListener {
            okButtonClicked()
        }

        mView.close_button.setOnClickListener {
            dismiss()
        }

        mView.reset_button.setOnClickListener {
            dismiss()
        }

        return mView
    }

    private fun setupView() {
        mView.hlu_dialog_min.setText(mSensorThreshold?.thresholdVeryLow.toString())
        mView.hlu_dialog_low.setText(mSensorThreshold?.thresholdLow.toString())
        mView.hlu_dialog_medium.setText(mSensorThreshold?.thresholdMedium.toString())
        mView.hlu_dialog_high.setText(mSensorThreshold?.thresholdHigh.toString())
        mView.hlu_dialog_max.setText(mSensorThreshold?.thresholdVeryHigh.toString())
    }

    private fun okButtonClicked() {
        mSensorThreshold ?: return

        val min = getValue(mView.hlu_dialog_min)
        min?.let { mSensorThreshold!!.thresholdVeryLow = min }

        val low = getValue(mView.hlu_dialog_low)
        low?.let { mSensorThreshold!!.thresholdLow = low }

        val medium = getValue(mView.hlu_dialog_medium)
        medium?.let { mSensorThreshold!!.thresholdMedium = medium }

        val high = getValue(mView.hlu_dialog_high)
        high?.let { mSensorThreshold!!.thresholdHigh = high }

        val max = getValue(mView.hlu_dialog_max)
        max?.let { mSensorThreshold!!.thresholdVeryHigh = max }

        listener.onSensorThresholdChangedFromDialog(mSensorThreshold!!)

        dismiss()
    }

    private fun getValue(input: TextInputEditText): Int? {
        val stringValue = input.text.toString().trim()
        return stringValue.toInt()
    }
}
