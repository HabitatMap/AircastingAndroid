package pl.llp.aircasting.screens.search

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import pl.llp.aircasting.R
import pl.llp.aircasting.screens.sync.SyncActivity

class SearchFixedSessionsActivity : AppCompatActivity() {
    companion object {
        fun start(rootActivity: FragmentActivity?) {
            rootActivity ?: return

            val intent = Intent(rootActivity, SyncActivity::class.java)

            rootActivity.startActivity(intent)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_fixed_sessions)
    }
}