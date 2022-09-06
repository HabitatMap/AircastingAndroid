package pl.llp.aircasting.ui.view.screens.dashboard

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.share_session_bottom_sheet.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.util.events.SessionsSyncEvent
import pl.llp.aircasting.util.extensions.*

class ShareSessionBottomSheet(
    private val mListener: Listener,
    val session: Session,
    private val mContext: Context?
) : BottomSheet() {
    interface Listener {
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
    private var shareFileButton: Button? = null
    private var loader: ImageView? = null
    lateinit var chosenSensor: String

    override fun layoutId(): Int {
        return R.layout.share_session_bottom_sheet
    }

    override fun setup() {
        EventBus.getDefault().safeRegister(this)

        expandBottomSheet()

        emailInputLayout = contentView?.email_text_input_layout
        emailInput = contentView?.email_input
        radioGroup = contentView?.stream_choose_radio_group

        val selectStreamTextView = contentView?.select_stream_text_view
        val emailCsvTextView = contentView?.email_csv_text_view
        val shareLinkButton = contentView?.share_link_button

        shareFileButton = contentView?.share_file_button
        shareFileButton?.setOnClickListener {
            shareFilePressed()
        }

        loader = contentView?.loader

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

    @Subscribe(sticky = true)
    fun onMessageEvent(sync: SessionsSyncEvent) = Handler(Looper.getMainLooper()).post {
        if (sync.inProgress) {
            shareFileButton?.isEnabled = false
            shareFileButton?.text = mContext?.getString(R.string.sync_in_progress)
            loader?.apply {
                startAnimation()
                visible()
            }
        } else {
            shareFileButton?.isEnabled = true
            shareFileButton?.text = mContext?.getString(R.string.share_file)
            loader?.apply {
                stopAnimation()
                inVisible()
            }
        }
    }

    private fun shareFilePressed() {
        val emailInput = emailInput?.text.toString().trim()
        if (!session.locationless) {
            if (!isValidEmail(emailInput)) {
                showError()
                return
            }
        }
        mListener.onShareFilePressed(session, emailInput)
        dismiss()
    }

    private fun showError() {
        emailInputLayout?.error = " "
        mContext?.showToast(getString(R.string.provided_email_is_not_correct), Toast.LENGTH_LONG)
    }

    fun shareLinkPressed() {
        mListener.onShareLinkPressed(session, chosenSensor)
        dismiss()
    }

    private fun setRadioButtonsForChosenSession() {
        fieldValues.clear()
        val currentSessionStreams = session.activeStreams
        currentSessionStreams.forEach { stream ->
            setRadioButtonProperties(stream)
        }
        radioGroup?.check(fieldValues.keys.minOrNull() ?: 0)
        chosenSensor = fieldValues[fieldValues.keys.minOrNull()]?.sensorName.toString()
    }

    private fun setRadioButtonProperties(stream: MeasurementStream) {
        val radioButton = RadioButton(context)
        val radioButtonPaddingLeft =
            context?.resources?.getDimension(R.dimen.keyline_4)?.toInt() ?: 0
        val radioButtonPaddingBottom =
            context?.resources?.getDimension(R.dimen.keyline_2)?.toInt() ?: 0
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        val drawable =
            context?.let { AppCompatResources.getDrawable(it, R.drawable.aircasting_radio_button) }
        radioButton.id = View.generateViewId()
        radioButton.text = stream.detailedType
        radioButton.layoutParams = layoutParams
        context?.let { radioButton.setAppearance(it, R.style.TextAppearance_Aircasting_Body1) }
        radioButton.gravity = Gravity.TOP
        radioButton.buttonDrawable = drawable
        radioButton.setBackgroundColor(Color.TRANSPARENT)
        radioButton.setPadding(radioButtonPaddingLeft, 0, 0, radioButtonPaddingBottom)
        radioGroup?.addView(radioButton)
        fieldValues[radioButton.id] = CurrentSessionStreams(stream.sensorName, stream.detailedType)
    }
}