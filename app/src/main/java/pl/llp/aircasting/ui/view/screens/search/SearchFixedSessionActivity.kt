package pl.llp.aircasting.ui.view.screens.search

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.app_bar.view.*
import pl.llp.aircasting.R
import pl.llp.aircasting.databinding.ActivityMainSearchFixedSessionsBinding
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.ui.view.fragments.SearchFollowLocationFragment
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.util.extensions.visible

class SearchFixedSessionActivity : BaseActivity() {

    companion object {
        fun start(rootActivity: FragmentActivity?) {
            rootActivity ?: return

            val intent = Intent(rootActivity, SearchFixedSessionActivity::class.java)
            rootActivity.startActivity(intent)
        }
    }

    lateinit var binding: ActivityMainSearchFixedSessionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_search_fixed_sessions)

        setupUI()
        showSearchFragment(savedInstanceState)
    }

    private fun setupUI() {
        binding.include.visible()
        binding.include.finishView.setOnClickListener { goToDashboard() }
    }

    private fun showSearchFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, SearchFollowLocationFragment())
                .commit()
        }
    }

    private fun goToDashboard() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}