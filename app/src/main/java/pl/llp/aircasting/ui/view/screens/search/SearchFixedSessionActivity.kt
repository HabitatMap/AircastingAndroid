package pl.llp.aircasting.ui.view.screens.search

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.databinding.ActivitySearchFixedSessionsBinding
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.ui.view.fragments.search_follow_fixed_session.SearchLocationFragment
import pl.llp.aircasting.ui.viewmodel.SearchFollowViewModel
import javax.inject.Inject

class SearchFixedSessionActivity : BaseActivity() {

    companion object {
        fun start(rootActivity: FragmentActivity?) {
            rootActivity ?: return

            val intent = Intent(rootActivity, SearchFixedSessionActivity::class.java)
            rootActivity.startActivity(intent)
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var fragmentFactory: FragmentFactory

    lateinit var searchFollowViewModel: SearchFollowViewModel

    lateinit var binding: ActivitySearchFixedSessionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_fixed_sessions)

        setupFactory()
        showSearchFragment(savedInstanceState)
    }

    private fun setupFactory() {
        (application as AircastingApplication)
            .userDependentComponent?.inject(this)
        searchFollowViewModel =
            ViewModelProvider(this, viewModelFactory)[SearchFollowViewModel::class.java]
        supportFragmentManager.fragmentFactory = fragmentFactory
    }

    private fun showSearchFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SearchLocationFragment::class.java, null, "searchLocation")
                .commit()
        }
    }
}