package io.lunarlogic.aircasting.screens.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.Session


class DeleteSessionBottomSheet(private val mListener: Listener, private val session: Session): BottomSheetDialogFragment() {
    interface Listener {
        fun onDeleteStreamsPressed(session: Session)
    }
    private var mStreamsOptionsContainer: LinearLayout? = null
    private var checkBoxMap: HashMap<CheckBox, DeleteStreamOption> = HashMap()
    private lateinit var allStreamsCheckbox: CheckBox

    class DeleteStreamOption(
        val stream: MeasurementStream
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

        allStreamsCheckboxListener()
        return view
    }

    private fun allStreamsCheckboxListener() {
        allStreamsCheckbox.setOnCheckedChangeListener { checkboxView, _ ->
            if (checkboxView.isChecked) {
                setAllCheckboxesState(true)
            } else {
                setAllCheckboxesState(false)
            }
        }
    }

    private fun setAllCheckboxesState(state: Boolean) {
        checkBoxMap.forEach { (checkbox, _) ->
            checkbox.isChecked = state
        }
    }

    fun getStreamsToDelete(): List<MeasurementStream> {
        return selectedOptions().map { option -> option.stream }
    }

    fun allStreamsBoxSelected(): Boolean {
        return allStreamsCheckbox.isChecked
    }

    private fun selectedOptions(): ArrayList<DeleteStreamOption> {
        val selectedOptions = checkBoxMap.filter { (key, _) -> (key.isChecked) }
        return ArrayList(selectedOptions.values)
    }

    private fun generateStreamsOptions() {
        val wholeSessionCheckboxTitle = resources.getString(R.string.delete_all_data_from_session)
        allStreamsCheckbox = CheckBox(context)
        val wholeSessionCheckboxView = createCheckboxView(allStreamsCheckbox, wholeSessionCheckboxTitle)
        val separatingLineView = createSeparatingLineView()
      
        mStreamsOptionsContainer?.addView(wholeSessionCheckboxView)
        mStreamsOptionsContainer?.addView(separatingLineView)

        val sessionStreams = session.activeStreams
        sessionStreams.forEach { stream ->
            val singleStreamCheckboxTitle = stream.detailedType
            val streamCheckbox = CheckBox(context)
            val streamCheckboxView = createCheckboxView(streamCheckbox, singleStreamCheckboxTitle)
            checkBoxMap[streamCheckbox] = DeleteStreamOption(stream)
            mStreamsOptionsContainer?.addView(streamCheckboxView)
        }
    }

    private fun createCheckboxView(checkbox: CheckBox, displayedValue: String?): View {
        checkbox.id = View.generateViewId()
        checkbox.text = displayedValue
        val buttonPaddingLeft = context?.resources?.getDimension(R.dimen.keyline_4)?.toInt() ?: 0
        val radioButtonPaddingTopBottom = context?.resources?.getDimension(R.dimen.keyline_2)?.toInt() ?: 0

        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val drawable = context?.getDrawable(R.drawable.checkbox_selector)

        layoutParams.leftMargin = 10
        layoutParams.bottomMargin = 10

        checkbox.setPadding(buttonPaddingLeft, radioButtonPaddingTopBottom, 0, radioButtonPaddingTopBottom)

        checkbox.layoutParams = layoutParams
        checkbox.buttonDrawable = drawable
        checkbox.background = context?.getDrawable(R.drawable.checkbox_background_selector)

        TextViewCompat.setTextAppearance(checkbox, R.style.TextAppearance_Aircasting_Checkbox)

        return checkbox
    }

    private fun createSeparatingLineView(): View {
        val view = View(context)
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 6)
        layoutParams.bottomMargin = 32
        layoutParams.topMargin = 8
        layoutParams.leftMargin = 8
        view.setBackgroundColor(ResourcesCompat.getColor(view.context.resources, R.color.aircasting_grey_50, null))
        view.layoutParams = layoutParams

        return view
    }
}

