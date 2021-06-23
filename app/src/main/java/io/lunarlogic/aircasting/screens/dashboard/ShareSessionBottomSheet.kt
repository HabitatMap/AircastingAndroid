package io.lunarlogic.aircasting.screens.dashboard

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.ValidationHelper
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.Session
import io.lunarlogic.aircasting.screens.common.BottomSheet
import kotlinx.android.synthetic.main.share_session_bottom_sheet.view.*

class ShareSessionBottomSheet(
    private val mListener: Listener,
    val session: Session,
    private val mContext: Context?
): BottomSheet() {
    interface Listener{
        fun onShareLinkPressed(session: Session, sensor: String)
        fun onShareFilePressed(session: Session, emailInput: String)
    }

    class CurrentSessionStreams(
        val sensorName: String,
        val detailedType: String?
    )

    val fieldValues = hashMapOf<Int, CurrentSessionStreams>()
    var emailInputLayout: TextInputLayout? = null
    private var emailInput: EditText? = null
    private var radioGroup: RadioGroup? = null
    lateinit var chosenSensor: String

    override fun layoutId(): Int {
        return R.layout.share_session_bottom_sheet
    }

    override fun setup() {
        expandBottomSheet()
        
        emailInputLayout = contentView?.email_text_input_layout
        emailInput = contentView?.email_input
        radioGroup = contentView?.stream_choose_radio_group

        val selectStreamTextView = contentView?.select_stream_text_view
        val emailCsvTextView = contentView?.email_csv_text_view
        val shareLinkButton = contentView?.share_link_button

        val shareFileButton = contentView?.share_file_button
        shareFileButton?.setOnClickListener {
            shareFilePressed()
        }

        val cancelButton = contentView?.cancel_button
        cancelButton?.setOnClickListener {
            dismiss()
        }

        val closeButton = contentView?.close_button
        closeButton?.setOnClickListener {
            dismiss()
        }

        if (session.locationless) {
            radioGroup?.visibility = View.GONE
            shareLinkButton?.visibility = View.GONE
            selectStreamTextView?.visibility = View.GONE
            emailInput?.visibility = View.GONE
            emailCsvTextView?.text = getString(R.string.generate_csv_file_without_share_link)
        } else {
            setRadioButtonsForChosenSession()

            radioGroup?.setOnCheckedChangeListener { group, checkedId ->
                chosenSensor = fieldValues[checkedId]?.sensorName.toString()
            }

            shareLinkButton?.setOnClickListener {
                shareLinkPressed()
            }
        }
    }

    private fun shareFilePressed() {
        val emailInput = emailInput?.text.toString().trim()
        if (!session.locationless) {
            if (!ValidationHelper.isValidEmail(emailInput)) {
                showError()
                return
            }
        }
        mListener.onShareFilePressed(session, emailInput)
        dismiss()
    }

    private fun showError() {
        emailInputLayout?.error = " "
        Toast.makeText(mContext, getString(R.string.provided_email_is_not_correct), Toast.LENGTH_LONG).show()
    }

    fun shareLinkPressed(){
        mListener.onShareLinkPressed(session, chosenSensor)
        dismiss()
    }

    private fun setRadioButtonsForChosenSession(){
        fieldValues.clear()
        val currentSessionStreams = session.activeStreams
        currentSessionStreams.forEach { stream ->
            setRadioButtonProperties(stream)
        }
        radioGroup?.check(fieldValues.keys.min() ?: 0)
        chosenSensor = fieldValues[fieldValues.keys.min()]?.sensorName.toString()
    }

    private fun setRadioButtonProperties(stream: MeasurementStream){
        val radioButton = RadioButton(context)
        val radioButtonPaddingLeft = context?.resources?.getDimension(R.dimen.keyline_4)?.toInt() ?: 0
        val radioButtonPaddingBottom = context?.resources?.getDimension(R.dimen.keyline_2)?.toInt() ?: 0
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        val drawable = context?.getDrawable(R.drawable.aircasting_radio_button)

        radioButton.id = View.generateViewId()
        radioButton.text = stream.detailedType
        radioButton.layoutParams = layoutParams
        radioButton.setTextAppearance(context, R.style.TextAppearance_Aircasting_Body1)
        radioButton.gravity = Gravity.TOP
        radioButton.buttonDrawable = drawable
        radioButton.setBackgroundColor(Color.TRANSPARENT)
        radioButton.setPadding( radioButtonPaddingLeft, 0, 0, radioButtonPaddingBottom)
        radioGroup?.addView(radioButton)
        fieldValues[radioButton.id] = CurrentSessionStreams(stream.sensorName, stream.detailedType)
    }
}
