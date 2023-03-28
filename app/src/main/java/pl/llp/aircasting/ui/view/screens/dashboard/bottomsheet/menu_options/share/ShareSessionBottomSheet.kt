package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.share

import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.share_session_bottom_sheet.view.*
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.api.services.CSVGenerationService
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.CSVHelper
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.ShareHelper
import pl.llp.aircasting.util.events.ExportSessionEvent
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.exceptions.SessionUploadPendingError
import pl.llp.aircasting.util.extensions.*
import javax.inject.Inject

class ShareSessionBottomSheet(
    private var mSession: Session?
) : BottomSheet() {
    constructor() : this(null)

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

    private val mSessionsViewModel by activityViewModels<SessionsViewModel>()

    @Inject
    lateinit var mErrorHandler: ErrorHandler

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var sessionsSyncService: SessionsSyncService

    override fun layoutId(): Int {
        return R.layout.share_session_bottom_sheet
    }

    override fun setup() {
        (requireActivity().application as AircastingApplication).userDependentComponent?.inject(this)

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

        lifecycleScope.launch {
            mSession?.let { session ->
                mSessionsViewModel.reloadSessionWithMeasurements(session.uuid).collect { dbSession ->
                    mSession = dbSession?.let { Session(it) }

                    if (mSession?.locationless == true) {
                        radioGroup?.visibility = View.GONE
                        shareLinkButton?.visibility = View.GONE
                        selectStreamTextView?.visibility = View.GONE
                        emailInput?.visibility = View.GONE
                        emailCsvTextView?.text =
                            getString(R.string.generate_csv_file_without_share_link)
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
            }

            sessionsSyncService.syncStatus.collect { syncStatus ->
                when (syncStatus) {
                    SessionsSyncService.Status.InProgress -> {
                        shareFileButton?.isEnabled = false
                        shareFileButton?.text = context?.getString(R.string.sync_in_progress)
                        loader?.apply {
                            startAnimation()
                            visible()
                        }
                    }
                    SessionsSyncService.Status.Idle -> {
                        shareFileButton?.isEnabled = true
                        shareFileButton?.text = context?.getString(R.string.share_file)
                        loader?.apply {
                            stopAnimation()
                            inVisible()
                        }
                    }
                }
            }
        }
    }

    private fun shareFilePressed() {
        val session = mSession ?: return

        val emailInput = emailInput?.text.toString().trim()
        if (session.locationless && !isValidEmail(emailInput)) {
            showError()
            return
        }

        if (session.locationless) {
            shareLocalFile(session)
        } else {
            val event = ExportSessionEvent(session, emailInput)
            EventBus.getDefault().post(event)
        }
        dismiss()
    }

    private fun shareLocalFile(session: Session) {
        context ?: return

        CSVGenerationService(
            session,
            requireActivity().applicationContext,
            CSVHelper(),
            mErrorHandler
        ).start()
    }

    private fun showError() {
        emailInputLayout?.error = " "
        requireActivity().showToast(
            getString(R.string.provided_email_is_not_correct),
            Toast.LENGTH_LONG
        )
    }

    fun shareLinkPressed() {
        if (mSession?.urlLocation != null) {
            openShareIntentChooser()
        } else {
            mErrorHandler.handleAndDisplay(SessionUploadPendingError())
        }
        dismiss()
    }

    private fun openShareIntentChooser() {
        val session = mSession ?: return

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, ShareHelper.shareLink(session, chosenSensor, context))
            putExtra(Intent.EXTRA_SUBJECT, context?.getString(R.string.share_title))
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, context?.getString(R.string.share_link))
        context?.startActivity(chooser)
    }

    private fun setRadioButtonsForChosenSession() {
        fieldValues.clear()
        val currentSessionStreams = mSession?.activeStreams
        currentSessionStreams?.forEach { stream ->
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