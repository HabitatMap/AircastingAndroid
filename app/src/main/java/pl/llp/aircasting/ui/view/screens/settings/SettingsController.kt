package pl.llp.aircasting.ui.view.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.params.UserSettingsBody
import pl.llp.aircasting.data.api.params.UserSettingsData
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.Authenticated
import pl.llp.aircasting.data.local.LogoutService
import pl.llp.aircasting.ui.view.common.BaseController
import pl.llp.aircasting.ui.view.screens.dashboard.ConfirmDangerActionDescriptionDialog
import pl.llp.aircasting.ui.view.screens.login.LoginService
import pl.llp.aircasting.ui.view.screens.settings.clear_sd_card.ClearSDCardActivity
import pl.llp.aircasting.ui.view.screens.settings.my_account.MyAccountActivity
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.KeepScreenOnToggledEvent
import pl.llp.aircasting.util.extensions.adjustMenuVisibility

@AssistedFactory
interface SettingsControllerFactory {
    fun create(
        mRootActivity: FragmentActivity?,
        mContext: Context?,
        viewMvc: SettingsViewMvcImpl?,
        fragmentManager: FragmentManager,
        coroutineScope: CoroutineScope?,
    ): SettingsController
}
class SettingsController @AssistedInject constructor(
    @Assisted private val mRootActivity: FragmentActivity?,
    @Assisted private val mContext: Context?,
    @Assisted var viewMvc: SettingsViewMvcImpl?,
    @Assisted private val fragmentManager: FragmentManager,
    @Assisted private val coroutineScope: CoroutineScope? = mRootActivity?.lifecycleScope,
    private val mSettings: Settings,
    private val loginService: LoginService,
    private val logoutService: LogoutService,
    @Authenticated private val apiService: ApiService,
) : SettingsViewMvc.Listener,
    SettingsViewMvc.BackendSettingsDialogListener,
    SettingsViewMvc.MicrophoneSettingsDialogListener,
    BaseController<SettingsViewMvcImpl>(viewMvc) {

    fun onStart() {
        viewMvc?.registerListener(this)
        mRootActivity?.adjustMenuVisibility(false)
    }

    fun onStop() {
        viewMvc?.unregisterListener(this)
    }

    suspend fun getUserDormantStreamAlertState(): Boolean? {
        return loginService.getUser()?.sessionStoppedAlert
    }

    override fun onMyAccountClicked() {
        MyAccountActivity.start(mContext)
    }

    override fun onBackendSettingsClicked() {
        startBackendSettingsDialog()
    }

    override fun onToggleThemeChange(switchButton: CompoundButton?) {
        if (mSettings.mobileActiveSessionsCount() > 0)
            ConfirmDangerActionDescriptionDialog(
                fragmentManager,
                title = mContext?.getString(R.string.toggle_night_mode_change_caution_header),
                description = mContext?.getString(R.string.toggle_night_mode_change_caution_description)
            ) {
                toggleThemeSwitch(switchButton)
            }.show()
        else
            toggleThemeSwitch(switchButton)
    }

    private fun toggleThemeSwitch(switchButton: CompoundButton?) {
        if (mSettings.isDarkThemeEnabled())
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        mSettings.toggleThemeChange()

        switchButton?.isChecked = mSettings.isDarkThemeEnabled()
    }

    override fun onToggleKeepScreenOnEnabled() {
        mSettings.toggleKeepScreenOn()
        EventBus.getDefault().post(KeepScreenOnToggledEvent())
    }

    override fun onToggle24hourFormatEnabled() {
        mSettings.toggleUse24HourFormatEnabled()
    }

    override fun onToggleCelsiusScaleEnabled() {
        mSettings.toggleUseCelsiusScaleEnabled()
    }

    override fun onToggleCrowdMapEnabled() {
        mSettings.toggleCrowdMapEnabled()
    }

    override fun onToggleDormantStreamAlert(enabled: Boolean) {
        coroutineScope?.launch {
            apiService.updateUserSettings(UserSettingsBody(UserSettingsData(enabled)))
        }

        saveDormantStreamAlertState(enabled)
    }

    fun saveDormantStreamAlertState(enabled: Boolean) {
        mSettings.toggleDormantStreamAlert(enabled)
    }

    override fun onToggleMapsEnabled() {
        mSettings.toggleMapSettingsEnabled()
    }

    override fun onToggleSatelliteViewEnabled() {
        mSettings.toggleUsingSatelliteView()
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
        mSettings.saveUrlAndPort(urlValue, portValue)
        logoutService.logout()
    }

    override fun confirmMicrophoneSettingsClicked(calibration: Int) {
        mSettings.microphoneSettingsChanged(calibration)
    }

    private fun startMicrophoneSettingsDialog() {
        val calibration = mSettings.getCalibrationValue()
        MicrophoneSettingsDialog(fragmentManager, calibration, this).show()
    }

    private fun startBackendSettingsDialog() {
        val url = mSettings.getBackendUrl()
        val port = mSettings.getBackendPort()
        BackendSettingsDialog(fragmentManager, url, port, this, mContext).show()
    }

}
