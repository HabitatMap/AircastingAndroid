package pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.theshold_alerts

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.recyclerview.widget.RecyclerView
import pl.llp.aircasting.databinding.CreateThresholdAlertItemBinding
import pl.llp.aircasting.ui.viewmodel.ThresholdAlertUiRepresentation
import pl.llp.aircasting.util.extensions.gone
import pl.llp.aircasting.util.extensions.visible

class ThresholdAlertsAdapter(
    private val alerts: List<ThresholdAlertUiRepresentation>,
) : RecyclerView.Adapter<ThresholdAlertsAdapter.ViewHolder>(), DefaultLifecycleObserver {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CreateThresholdAlertItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(alerts[position])
    }

    override fun getItemCount(): Int {
        return alerts.size
    }

    inner class ViewHolder(
        private val binding: CreateThresholdAlertItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var alert: ThresholdAlertUiRepresentation

        fun bind(alert: ThresholdAlertUiRepresentation) {
            this.alert = alert
            binding.alert = this.alert
            binding.onCheckChanged =
                CompoundButton.OnCheckedChangeListener { _, isChecked ->
                    if (isChecked)
                        binding.alertOptions.visible()
                    else
                        binding.alertOptions.gone()
                }
            binding.thresholdAlertValue.addTextChangedListener {
                val value = it.toString().toDoubleOrNull()
                if (value == null)
                    binding.thresholdAlertValueLayout.error = " "
                else
                    binding.thresholdAlertValueLayout.error = null
            }
        }
    }
}

