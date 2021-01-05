package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Session

class ShareSessionBottomSheet(
    private val mListener: ShareSessionBottomSheet.Listener,
    val session: Session
): BottomSheetDialogFragment() {
    interface Listener{
        fun onShareLinkPressed()
        fun onShareFilePressed(emailInput: String)
        fun onCancelPressed()
    }

    class CurrentSessionStreams(
        val sensorName: String,
        val detailedType: String?
    )

    private val TAG = "ShareSessionBottomSheet"

    val fieldValues = hashMapOf<Int, CurrentSessionStreams>()
    var emailInput: EditText? = null
    var radioGroup: RadioGroup? = null
    lateinit var chosenSensor: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.share_session_bottom_sheet, container, false)

        emailInput = view?.findViewById(R.id.email_input)
        radioGroup = view?.findViewById(R.id.stream_choose_radio_group)

        setRadioButtonsForChosenSession()

        radioGroup?.setOnCheckedChangeListener { group, checkedId ->
            chosenSensor = fieldValues[checkedId]?.sensorName.toString()
        }

        val shareLinkButton = view?.findViewById<Button>(R.id.share_link_button)
        shareLinkButton?.setOnClickListener {
            mListener.onShareLinkPressed()
        }

        val shareFileButton = view?.findViewById<Button>(R.id.share_file_button)
        shareFileButton?.setOnClickListener {
            shareFilePressed()
        }

        val cancelButton = view?.findViewById<Button>(R.id.cancel_button)
        cancelButton?.setOnClickListener {
            mListener.onCancelPressed()
        }

        val closeButton = view?.findViewById<ImageView>(R.id.close_button)
        closeButton?.setOnClickListener {
            mListener.onCancelPressed()
        }

        return view
    }

    fun show(manager: FragmentManager){
        show(manager, TAG)
    }

    fun shareFilePressed(){
        val emailInput = emailInput?.text.toString().trim()
        mListener.onShareFilePressed(emailInput)
    }

    private fun setRadioButtonsForChosenSession(){
        fieldValues.clear()
        val currentSessionStreams = session.streams
        currentSessionStreams.forEach{ stream ->
            val radioButton = RadioButton(context)
            radioButton.id = View.generateViewId()
            radioButton.text = stream.detailedType
            val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            radioButton.layoutParams = layoutParams
            radioGroup?.addView(radioButton)
            fieldValues[radioButton.id] = CurrentSessionStreams(stream.sensorName, stream.detailedType)
        }
    }
}
