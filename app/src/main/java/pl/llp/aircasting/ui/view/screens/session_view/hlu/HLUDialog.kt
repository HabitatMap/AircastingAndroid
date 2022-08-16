package pl.llp.aircasting.ui.view.screens.session_view.hlu

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.hlu_dialog.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.ui.view.common.BaseDialog
import pl.llp.aircasting.util.TemperatureConverter
import pl.llp.aircasting.util.extensions.labelFormat

class HLUDialog(
    private var mSensorThreshold: SensorThreshold?,
    private val mMeasurementStream: MeasurementStream?,
    mFragmentManager: FragmentManager,
    private val listener: HLUDialogListener
) : BaseDialog(mFragmentManager) {
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
            resetToDefaultsClicked()
        }

        return mView
    }

    private fun setupView() {
        mView.apply {
            setThresholdText(hlu_dialog_min, mSensorThreshold?.thresholdVeryLow)
            setThresholdText(hlu_dialog_low, mSensorThreshold?.thresholdLow)
            setThresholdText(hlu_dialog_medium, mSensorThreshold?.thresholdMedium)
            setThresholdText(hlu_dialog_high, mSensorThreshold?.thresholdHigh)
            setThresholdText(hlu_dialog_max, mSensorThreshold?.thresholdVeryHigh)
        }
    }

    private fun setThresholdText(label: TextView, threshold: Int?) {
        if (threshold != null) {
            label.text =
                if (mMeasurementStream?.isMeasurementTypeTemperature() == true
                    && TemperatureConverter.isCelsiusToggleEnabled()
                )
                    labelFormat(
                        TemperatureConverter.fahrenheitToCelsius(
                            threshold.toFloat()
                        )
                    )
                else
                    threshold.toString()
        }
    }

    private fun okButtonClicked() {
        mSensorThreshold ?: return

        val min = getValue(mView.hlu_dialog_min)
        val low = getValue(mView.hlu_dialog_low)
        val medium = getValue(mView.hlu_dialog_medium)
        val high = getValue(mView.hlu_dialog_high)
        val max = getValue(mView.hlu_dialog_max)

        if (validate(min, low, medium, high, max)) {
            min?.let { mSensorThreshold!!.thresholdVeryLow = min }
            low?.let { mSensorThreshold!!.thresholdLow = low }
            medium?.let { mSensorThreshold!!.thresholdMedium = medium }
            high?.let { mSensorThreshold!!.thresholdHigh = high }
            max?.let { mSensorThreshold!!.thresholdVeryHigh = max }

            listener.onSensorThresholdChangedFromDialog(mSensorThreshold!!)

            dismiss()
        } else {
            listener.onValidationFailed()
        }
    }

    private fun validate(min: Int?, low: Int?, medium: Int?, high: Int?, max: Int?): Boolean {
        return min != null && low != null && medium != null && high != null && max != null &&
                min < low && low < medium && medium < high && high < max
    }

    private fun resetToDefaultsClicked() {
        if (mSensorThreshold == null || mMeasurementStream == null) return

        mSensorThreshold?.apply {
            thresholdVeryLow = mMeasurementStream.thresholdVeryLow
            thresholdLow = mMeasurementStream.thresholdLow
            thresholdMedium = mMeasurementStream.thresholdMedium
            thresholdHigh = mMeasurementStream.thresholdHigh
            thresholdVeryHigh = mMeasurementStream.thresholdVeryHigh
        }

        listener.onSensorThresholdChangedFromDialog(mSensorThreshold!!)

        dismiss()
    }

    private fun getValue(input: TextInputEditText): Int? {
        val stringValue = input.text.toString().trim()
        if (stringValue.isEmpty()) return null

        val value = stringValue.toInt()
        return if (mMeasurementStream?.isMeasurementTypeTemperature() == true
            && mMeasurementStream.isDetailedTypeCelsius()
        )
            TemperatureConverter.celsiusToFahrenheit(value)
        else value
    }
}
