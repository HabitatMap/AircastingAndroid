package pl.llp.aircasting.ui.view.screens.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import pl.llp.aircasting.R

class MySettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)
    }
}