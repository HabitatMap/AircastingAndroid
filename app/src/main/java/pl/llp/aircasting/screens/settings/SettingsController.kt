package pl.llp.aircasting.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.R
import pl.llp.aircasting.events.KeepScreenOnToggledEvent
import pl.llp.aircasting.lib.Settings
import pl.llp.aircasting.screens.common.BaseController
import pl.llp.aircasting.screens.settings.clear_sd_card.ClearSDCardActivity
import pl.llp.aircasting.screens.settings.my_account.MyAccountActivity


class SettingsController(
    private val mRootActivity: FragmentActivity?,
    private val mContext: Context?,
    var viewMvc: SettingsViewMvcImpl?,
    private val mSettings: Settings,
    private val fragmentManager: FragmentManager
) : SettingsViewMvc.Listener,
    SettingsViewMvc.BackendSettingsDialogListener,
    SettingsViewMvc.MicrophoneSettingsDialogListener,
    BaseController<SettingsViewMvcImpl>(viewMvc) {

    fun onStart(){
        viewMvc?.registerListener(this)
    }

    fun onStop(){
        viewMvc?.unregisterListener(this)
    }

    override fun onMyAccountClicked() {
        MyAccountActivity.start(mContext)
    }

    override fun onBackendSettingsClicked() {
        startBackendSettingsDialog()
    }

    override fun onToggleKeepScreenOnEnabled() {
        mSettings.toggleKeepScreenOn()
        EventBus.getDefault().post(KeepScreenOnToggledEvent())
    }

    override fun onToggle24hourFormatEnabled() {
        mSettings.toggleUse24HourFormatEnabled()
    }

    override fun onToggleCrowdMapEnabled() {
        mSettings.toggleCrowdMapEnabled()
    }

    override fun onToggleMapsEnabled() {
        mSettings.toggleMapSettingsEnabled()
    }

    override fun onClearSDCardClicked() {
        ClearSDCardActivity.start(mRootActivity)
    }

    override fun onMicrophoneSettingsClicked() {
        startMicrophoneSettingsDialog()
    }

    override fun yourPrivacyClicked() {
        val uri: Uri = Uri.parse(mContext?.getString(R.string.your_privacy_link))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        mContext?.let { startActivity(it, intent, null) }
    }

    override fun confirmClicked(urlValue: String, portValue: String) {
        mSettings.backendSettingsChanged(urlValue, portValue)
    }

    override fun confirmMicrophoneSettingsClicked(calibration: Int) {
        mSettings.microphoneSettingsChanged(calibration)
    }

    private fun startMicrophoneSettingsDialog() {
        val calibration = mSettings.getCalibrationValue()
        MicrophoneSettingsDialog(fragmentManager, calibration, this).show()
    }

    fun startBackendSettingsDialog(){
        val url = mSettings.getBackendUrl()
        val port = mSettings.getBackendPort()
        BackendSettingsDialog(fragmentManager, url, port, this, mContext).show()
    }

}
