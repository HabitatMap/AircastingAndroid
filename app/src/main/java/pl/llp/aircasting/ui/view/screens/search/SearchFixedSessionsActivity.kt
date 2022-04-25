package pl.llp.aircasting.ui.view.screens.search

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import pl.llp.aircasting.R

class SearchFixedSessionsActivity : AppCompatActivity() {
    companion object {
        fun start(rootActivity: FragmentActivity?) {
            rootActivity ?: return

            val intent = Intent(rootActivity, SearchFixedSessionsActivity::class.java)
            rootActivity.startActivity(intent)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_search_fixed_sessions)
    }
}