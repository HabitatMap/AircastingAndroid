package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.delete

import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import kotlinx.android.synthetic.main.delete_session_bottom_sheet.view.*
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.ConfirmDangerActionDialog
import pl.llp.aircasting.util.events.DeleteSessionEvent
import pl.llp.aircasting.util.events.DeleteStreamsEvent


class DeleteSessionBottomSheet(
    private val session: Session?
) : BottomSheet() {
    constructor() : this(null)

    private var mStreamsOptionsContainer: LinearLayout? = null
    private var checkBoxMap: HashMap<CheckBox, DeleteStreamOption> = HashMap()
    private lateinit var allStreamsCheckbox: CheckBox

    class DeleteStreamOption(
        val stream: MeasurementStream
    )

    override fun layoutId(): Int {
        return R.layout.delete_session_bottom_sheet
    }

    override fun setup() {
        expandBottomSheet()

        val deleteStreamsButton = contentView?.delete_streams_button
        val cancelButton = contentView?.cancel_button
        val closeButton = contentView?.close_button

        cancelButton?.setOnClickListener {
            dismiss()
        }

        closeButton?.setOnClickListener {
            dismiss()
        }

        deleteStreamsButton?.setOnClickListener {
            val streamsToDelete = getStreamsToDelete()
            if (deleteAllStreamsSelected(
                    allStreamsBoxSelected(),
                    getStreamsToDelete().size,
                    session?.streams?.size
                )
            ) {
                ConfirmDangerActionDialog(parentFragmentManager) {
                    deleteSession(session?.uuid)
                }
                    .show()
            } else {
                ConfirmDangerActionDialog(parentFragmentManager) {
                    deleteStreams(session, streamsToDelete)
                }
                    .show()
            }
        }

        mStreamsOptionsContainer = contentView?.streams_options_container
        generateStreamsOptions()

        setAllStreamsCheckboxListener()
    }

    private fun deleteSession(sessionUUID: String?) {
        sessionUUID ?: return

        val event = DeleteSessionEvent(sessionUUID)
        EventBus.getDefault().post(event)
        dismiss()
    }

    private fun deleteStreams(session: Session?, streamsToDelete: List<MeasurementStream>?) {
        session ?: return

        val event = DeleteStreamsEvent(session, streamsToDelete)
        EventBus.getDefault().post(event)
        dismiss()
    }

    private fun deleteAllStreamsSelected(
        allStreamsBoxSelected: Boolean,
        selectedOptionsCount: Int?,
        sessionStreamsCount: Int?
    ): Boolean {
        return (allStreamsBoxSelected) || (selectedOptionsCount == sessionStreamsCount)
    }

    fun setAllStreamsCheckboxListener() {
        allStreamsCheckbox.setOnCheckedChangeListener { checkboxView, _ ->
            setAllCheckboxesState(checkboxView.isChecked)
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
        val wholeSessionCheckboxView =
            createCheckboxView(allStreamsCheckbox, wholeSessionCheckboxTitle)
        val separatingLineView = createSeparatingLineView()

        mStreamsOptionsContainer?.addView(wholeSessionCheckboxView)
        mStreamsOptionsContainer?.addView(separatingLineView)

        val sessionStreams = session?.activeStreams
        sessionStreams?.forEach { stream ->
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
        val radioButtonPaddingTopBottom =
            context?.resources?.getDimension(R.dimen.keyline_2)?.toInt() ?: 0

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val drawable =
            context?.let { AppCompatResources.getDrawable(it, R.drawable.checkbox_selector) }

        layoutParams.leftMargin = 10
        layoutParams.bottomMargin = 10

        checkbox.setPadding(
            buttonPaddingLeft,
            radioButtonPaddingTopBottom,
            0,
            radioButtonPaddingTopBottom
        )

        checkbox.layoutParams = layoutParams
        checkbox.buttonDrawable = drawable
        checkbox.background = context?.let {
            AppCompatResources.getDrawable(
                it,
                R.drawable.checkbox_background_selector
            )
        }

        TextViewCompat.setTextAppearance(checkbox, R.style.TextAppearance_Aircasting_Checkbox)

        return checkbox
    }

    private fun createSeparatingLineView(): View {
        val view = View(context)
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 6)
        layoutParams.bottomMargin = 32
        layoutParams.topMargin = 8
        layoutParams.leftMargin = 8
        view.setBackgroundColor(
            ResourcesCompat.getColor(
                view.context.resources,
                R.color.aircasting_grey_50,
                null
            )
        )
        view.layoutParams = layoutParams

        return view
    }
}

