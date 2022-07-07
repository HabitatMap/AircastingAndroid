package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.FragmentActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_dashboard.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.common.BatteryAlertDialog
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardController
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardPagerAdapter
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardViewMvcImpl
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.adjustMenuVisibility
import pl.llp.aircasting.util.isIgnoringBatteryOptimizations
import pl.llp.aircasting.util.isSDKGreaterOrEqualToM
import javax.inject.Inject

class DashboardFragment : BaseFragment<DashboardViewMvcImpl, DashboardController>() {

    @Inject
    lateinit var settings: Settings

    private var mTabPosition: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        val pagerAdapter = context?.let { DashboardPagerAdapter(it, childFragmentManager) }
        view = pagerAdapter?.let {
            DashboardViewMvcImpl(
                inflater, container, childFragmentManager,
                it, DashboardPagerAdapter.TABS_COUNT
            )
        }
        controller = DashboardController(view)
        val tabId = arguments?.get("tabId") as Int?
        controller?.onCreate(tabId)

        handleBackButtonPress()
        return view?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupReorder()
    }

    private fun setupReorder() {
        tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                isAdjustable(tab.position)
                mTabPosition = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                isAdjustable(tab.position)
                mTabPosition = tab.position
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                isAdjustable(tab.position)
                mTabPosition = tab.position
            }
        })
    }

    private fun isAdjustable(position: Int) {
        activity?.let {
            if (position == 0)
                adjustMenuVisibility(it, true, settings.getFollowedSessionsNumber())
            else adjustMenuVisibility(it, false)
        }
    }

    private fun handleBackButtonPress() {
        requireActivity().let {
            it.onBackPressedDispatcher
                .addCallback(viewLifecycleOwner) {

                    if (isEnabled && mTabPosition == 0) {
                        isEnabled = false
                        showBatteryOptimizationDialogIfNeeded()
                    } else it.onBackPressed()

                }
        }
    }

    private fun showBatteryOptimizationDialogIfNeeded() {
        if (isSDKGreaterOrEqualToM() && !isIgnoringBatteryOptimizations(requireContext())) showBatteryOptimizationHelperDialog(
            requireActivity()
        )
    }

    private fun showBatteryOptimizationHelperDialog(fragmentActivity: FragmentActivity) {
        BatteryAlertDialog(
            fragmentActivity.supportFragmentManager,
            fragmentActivity.getString(R.string.running_background),
            fragmentActivity.getString(R.string.battery_desc)
        ).show()
    }
}
