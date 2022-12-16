package pl.llp.aircasting.ui.view.screens.dashboard.theshold_alerts

import android.util.Log
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.databinding.CreateThresholdAlertBottomSheetLayoutBinding
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.viewmodel.CreateThresholdAlertBottomSheetViewModel
import pl.llp.aircasting.ui.viewmodel.ThresholdAlertUiRepresentation
import pl.llp.aircasting.util.extensions.showToast
import pl.llp.aircasting.util.extensions.startAnimation
import pl.llp.aircasting.util.extensions.stopAnimation
import pl.llp.aircasting.util.extensions.visible
import javax.inject.Inject

class CreateThresholdAlertBottomSheet(private val session: Session?) : BottomSheet() {
    constructor() : this(null)

    override fun layoutId() = R.layout.create_threshold_alert_bottom_sheet_layout

    private var binding: CreateThresholdAlertBottomSheetLayoutBinding? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: CreateThresholdAlertBottomSheetViewModel

    private lateinit var uiAlerts: List<ThresholdAlertUiRepresentation>

    override fun setup() {
        super.setup()
        expandBottomSheet()
        (activity?.application as AircastingApplication).appComponent.inject(this)

        binding = contentView?.let { CreateThresholdAlertBottomSheetLayoutBinding.bind(it) }
        viewModel = ViewModelProvider(
            this,
            viewModelFactory
        )[CreateThresholdAlertBottomSheetViewModel::class.java]
        binding?.apply {
            lifecycleOwner = this@CreateThresholdAlertBottomSheet
            view = this@CreateThresholdAlertBottomSheet
            viewModel = this@CreateThresholdAlertBottomSheet.viewModel
            loader.startAnimation()
        }
        displayAlerts()
    }

    private fun displayAlerts() = lifecycleScope.launch {
        viewModel.getAlertsForDisplaying(session)
            .flowOn(Dispatchers.Default)
            .collect { result ->
                result
                    .onSuccess { alerts ->
                        uiAlerts = alerts
                        val adapter = ThresholdAlertsAdapter(uiAlerts)
                        binding?.alertsRecycler?.adapter = adapter
                    }
                    .onFailure {
                        Log.e("alerts", it.stackTrace.toString())
                        context?.showToast("Something went wrong")
                    }

                disableLoadingAndShowContent()
            }
    }

    private fun disableLoadingAndShowContent() {
        binding?.alertsRecycler?.doOnNextLayout {
            lifecycleScope.launch {
                binding?.loader?.stopAnimation()
                // Small delay smooths out glitchy rendering for recycler view
                delay(10)
                binding?.content?.visible()
            }
        }
    }

    fun close(view: View) {
        dismiss()
    }

    fun save(view: View) {
        if (formsAreValid()) {
            saveAlerts()
            close(view)
        }
        else
            context?.showToast("Please fill in threshold values!")
    }

    private fun saveAlerts() {
        val activity = requireActivity()
        activity.lifecycleScope.launch {
            viewModel.saveEditedAlerts(uiAlerts, session)
                .flowOn(Dispatchers.Default)
                .collect { result ->
                    result
                        .onSuccess { activity.showToast("Success") }
                        .onFailure { activity.showToast(it.message.toString()) }
                }
        }
    }

    private fun formsAreValid(): Boolean =
        uiAlerts.find { it.threshold == null && it.enabled } == null
}