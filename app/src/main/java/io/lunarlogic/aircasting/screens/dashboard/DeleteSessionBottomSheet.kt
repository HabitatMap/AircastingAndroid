package io.lunarlogic.aircasting.screens.dashboard

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.widget.TextViewCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Session

class DeleteSessionBottomSheet(private val mListener: Listener, private val session: Session): BottomSheetDialogFragment() {
    interface Listener {
        fun onDeleteStreamsPressed(sessionUUID: String)
        fun onCancelDeleteSessionDialogPressed()
    }
    private var mStreamsOptionsContainer: LinearLayout? = null
    private var checkBoxMap: HashMap<CheckBox, Option> = HashMap()

    class Option(
        val sessionUUID: String,
        val allStreamsBoxSelected: Boolean,
        val streamName: String? = null
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.delete_session_bottom_sheet, container, false)

        val cancelButton = view?.findViewById<Button>(R.id.cancel_button)
        cancelButton?.setOnClickListener {
            mListener.onCancelDeleteSessionDialogPressed()
        }

        val closeButton = view?.findViewById<ImageView>(R.id.close_button)
        closeButton?.setOnClickListener {
            mListener.onCancelDeleteSessionDialogPressed()
        }

        val deleteStreamsButton = view?.findViewById<Button>(R.id.delete_streams_button)
        deleteStreamsButton?.setOnClickListener {
            mListener.onDeleteStreamsPressed(session.uuid)
        }

        mStreamsOptionsContainer = view?.findViewById(R.id.streams_options_container)
        generateStreamsOptions()

        checkBoxMap.forEach { (checkbox, option) ->
            checkbox.setOnCheckedChangeListener { _, _ -> }
        }
        return view
    }

    fun getSelectedValues(): ArrayList<Option> {
        return selectedValues()
    }

    fun getSessionUUID(): String {
        return session.uuid
    }

    fun allStreamsBoxSelected(): Boolean {
        return selectedValues().any { it.allStreamsBoxSelected }
    }

    private fun generateStreamsOptions() {
        val wholeSessionCheckboxTitle = resources.getString(R.string.delete_all_data_from_session)
        val checkbox = CheckBox(context)
        val wholeSessionCheckboxView = createCheckboxView(checkbox, wholeSessionCheckboxTitle)
        checkBoxMap[checkbox] = Option(session.uuid, true)
        mStreamsOptionsContainer?.addView(wholeSessionCheckboxView)

        val sessionStreams = session.streams
        sessionStreams.forEach { stream ->
            val singleStreamCheckboxTitle = stream.detailedType
            val streamCheckbox = CheckBox(context)
            val streamCheckboxView = createCheckboxView(streamCheckbox, singleStreamCheckboxTitle)
            checkBoxMap[streamCheckbox] = Option(session.uuid, false, stream.sensorName)
            mStreamsOptionsContainer?.addView(streamCheckboxView)
        }
    }

    private fun createCheckboxView(checkbox: CheckBox, displayedValue: String?): View {
        checkbox.id = View.generateViewId()
        checkbox.text = displayedValue
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        checkbox.layoutParams = layoutParams
        checkbox.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.aircasting_blue_400))
        TextViewCompat.setTextAppearance(checkbox, R.style.TextAppearance_Aircasting_Checkbox)
        return checkbox
    }

    private fun selectedValues(): ArrayList<Option> {
        val values = ArrayList<Option>()
        if (checkBoxMap.isEmpty()) {
            return values
        }
        for ((key, value) in checkBoxMap) {
            if (key.isChecked) {
                values.add(value)
            }
        }
        return values
    }
}
