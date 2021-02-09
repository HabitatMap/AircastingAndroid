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
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.Session
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DeleteSessionBottomSheet(private val mListener: Listener, private val session: Session): BottomSheetDialogFragment() {
    interface Listener {
        fun onDeleteStreamsPressed(session: Session)
    }
    private var mStreamsOptionsContainer: LinearLayout? = null
    private var checkBoxMap: HashMap<CheckBox, DeleteStreamOption> = HashMap()

    class DeleteStreamOption(
        val allStreamsBoxSelected: Boolean,
        val stream: MeasurementStream? = null
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.delete_session_bottom_sheet, container, false)
        val deleteStreamsButton = view?.findViewById<Button>(R.id.delete_streams_button)
        val cancelButton = view?.findViewById<Button>(R.id.cancel_button)
        val closeButton = view?.findViewById<ImageView>(R.id.close_button)

        cancelButton?.setOnClickListener {
            dismiss()
        }

        closeButton?.setOnClickListener {
            dismiss()
        }

        deleteStreamsButton?.setOnClickListener {
            mListener.onDeleteStreamsPressed(session)
        }

        mStreamsOptionsContainer = view?.findViewById(R.id.streams_options_container)
        generateStreamsOptions()

        checkBoxMap.forEach { (checkbox, option) ->
            checkbox.setOnCheckedChangeListener { _, _ -> }
        }
        return view
    }

    fun getStreamsToDelete(): List<MeasurementStream> {
        val streamsSelectedForDeletion: MutableList<MeasurementStream> = ArrayList()
        selectedOptions().forEach { option ->
            option.stream?.let { streamsSelectedForDeletion.add(it) }
        }
        return Collections.unmodifiableList(streamsSelectedForDeletion)
    }

    fun allStreamsBoxSelected(): Boolean {
        return selectedOptions().any { it.allStreamsBoxSelected }
    }

    private fun selectedOptions(): ArrayList<DeleteStreamOption> {
        val selectedOptions = checkBoxMap.filter { (key, _) -> (key.isChecked) }
        return ArrayList(selectedOptions.values)
    }

    private fun generateStreamsOptions() {
        val wholeSessionCheckboxTitle = resources.getString(R.string.delete_all_data_from_session)
        val checkbox = CheckBox(context)
        val wholeSessionCheckboxView = createCheckboxView(checkbox, wholeSessionCheckboxTitle)
        checkBoxMap[checkbox] = DeleteStreamOption(true)
        mStreamsOptionsContainer?.addView(wholeSessionCheckboxView)

        val sessionStreams = session.activeStreams
        sessionStreams.forEach { stream ->
            val singleStreamCheckboxTitle = stream.detailedType
            val streamCheckbox = CheckBox(context)
            val streamCheckboxView = createCheckboxView(streamCheckbox, singleStreamCheckboxTitle)
            checkBoxMap[streamCheckbox] = DeleteStreamOption(false, stream)
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
}

