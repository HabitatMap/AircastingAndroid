package pl.llp.aircasting.ui.view.screens.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pl.llp.aircasting.R

class MySettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, MySettingsFragment())
            .commit()
    }
}