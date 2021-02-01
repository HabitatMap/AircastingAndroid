package io.lunarlogic.aircasting.screens.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.lib.ValidationHelper
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.Session

class ShareSessionBottomSheet(
    private val mListener: ShareSessionBottomSheet.Listener,
    val session: Session,
    private val mContext: Context?
): BottomSheetDialogFragment() {
    interface Listener{
        fun onShareLinkPressed(session: Session, sensor: String)
        fun onShareFilePressed(session: Session, emailInput: String)
    }

    class CurrentSessionStreams(
        val sensorName: String,
        val detailedType: String?
    )

    private val TAG = "ShareSessionBottomSheet"

    val fieldValues = hashMapOf<Int, CurrentSessionStreams>()
    var emailInputLayout: TextInputLayout? = null
    private var emailInput: EditText? = null
    private var radioGroup: RadioGroup? = null
    lateinit var chosenSensor: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.share_session_bottom_sheet, container, false)

        emailInputLayout = view?.findViewById(R.id.email_text_input_layout)
        emailInput = view?.findViewById(R.id.email_input)
        radioGroup = view?.findViewById(R.id.stream_choose_radio_group)

        val selectStreamTextView = view?.findViewById<TextView>(R.id.select_stream_text_view)
        val emailCsvTextView = view?.findViewById<TextView>(R.id.email_csv_text_view)
        val shareLinkButton = view?.findViewById<Button>(R.id.share_link_button)

        val shareFileButton = view?.findViewById<Button>(R.id.share_file_button)
        shareFileButton?.setOnClickListener {
            shareFilePressed()
        }

        val cancelButton = view?.findViewById<Button>(R.id.cancel_button)
        cancelButton?.setOnClickListener {
            dismiss()
        }

        val closeButton = view?.findViewById<ImageView>(R.id.close_button)
        closeButton?.setOnClickListener {
            dismiss()
        }

        if (session.locationless) {
            radioGroup?.visibility = View.GONE
            shareLinkButton?.visibility = View.GONE
            selectStreamTextView?.visibility = View.GONE
            emailCsvTextView?.text = getString(R.string.email_csv_file_without_share_link)
        } else {
            setRadioButtonsForChosenSession()

            radioGroup?.setOnCheckedChangeListener { group, checkedId ->
                chosenSensor = fieldValues[checkedId]?.sensorName.toString()
            }

            shareLinkButton?.setOnClickListener {
                shareLinkPressed()
            }
        }

        return view
    }

    fun show(manager: FragmentManager){
        show(manager, TAG)
    }

    fun shareFilePressed(){
        val emailInput = emailInput?.text.toString().trim()
        if (!ValidationHelper.isValidEmail(emailInput)){
            showError()
            return
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
