package pl.llp.aircasting.ui.view.screens.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.fragments.MySettingsFragment
import pl.llp.aircasting.util.adjustMenuVisibility

class MySettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_settings)

        setupUI()
        setupFragmentTransaction()
    }

    private fun setupUI() {
        adjustMenuVisibility(this, false)
    }

    private fun setupFragmentTransaction() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, MySettingsFragment())
            .commit()
    }
}