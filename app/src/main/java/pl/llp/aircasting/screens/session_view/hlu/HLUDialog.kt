package pl.llp.aircasting.screens.session_view.hlu

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.hlu_dialog.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.lib.TemperatureConverter
import pl.llp.aircasting.lib.labelFormat
import pl.llp.aircasting.lib.temperatureFromFahrenheitToCelsius
import pl.llp.aircasting.models.MeasurementStream
import pl.llp.aircasting.models.SensorThreshold
import pl.llp.aircasting.screens.common.BaseDialog

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

        if (mMeasurementStream?.measurementType == "Temperature"
            && TemperatureConverter.isCelsiusToggleEnabled()
        ) mView.apply {
            hlu_dialog_min.setText(
                labelFormat(
                    temperatureFromFahrenheitToCelsius(
                        mMeasurementStream.thresholdVeryLow.toFloat()
                    )
                )
            )
            hlu_dialog_low.setText(
                labelFormat(
                    temperatureFromFahrenheitToCelsius(
                        mMeasurementStream.thresholdLow.toFloat()
                    )
                )
            )
            hlu_dialog_medium.setText(
                labelFormat(
                    temperatureFromFahrenheitToCelsius(
                        mMeasurementStream.thresholdMedium.toFloat()
                    )
                )
            )
            hlu_dialog_high.setText(
                labelFormat(
                    temperatureFromFahrenheitToCelsius(
                        mMeasurementStream.thresholdHigh.toFloat()
                    )
                )
            )
            hlu_dialog_max.setText(
                labelFormat(
                    temperatureFromFahrenheitToCelsius(
                        mMeasurementStream.thresholdVeryHigh.toFloat()
                    )
                )

            )
        } else mView.apply {
            hlu_dialog_min.setText(mSensorThreshold?.thresholdVeryLow.toString())
            hlu_dialog_low.setText(mSensorThreshold?.thresholdLow.toString())
            hlu_dialog_medium.setText(mSensorThreshold?.thresholdMedium.toString())
            hlu_dialog_high.setText(mSensorThreshold?.thresholdHigh.toString())
            hlu_dialog_max.setText(mSensorThreshold?.thresholdVeryHigh.toString())
        }

    }

    private fun okButtonClicked() {
        mSensorThreshold ?: return

        mSensorThreshold?.thresholdVeryLow = 0
        mSensorThreshold?.thresholdLow = 0
        mSensorThreshold?.thresholdMedium = 0
        mSensorThreshold?.thresholdHigh = 0
        mSensorThreshold?.thresholdVeryHigh = 0
        // reset the previous values

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

        return if (stringValue.isEmpty()) null else stringValue.toInt()
    }
}
