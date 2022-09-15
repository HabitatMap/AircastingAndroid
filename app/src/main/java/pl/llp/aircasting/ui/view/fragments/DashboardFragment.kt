package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.annotation.Nullable
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_dashboard.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.services.SessionsSyncService
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardController
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardPagerAdapter
import pl.llp.aircasting.ui.view.screens.dashboard.DashboardViewMvcImpl
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.extensions.adjustMenuVisibility
import pl.llp.aircasting.util.extensions.isIgnoringBatteryOptimizations
import pl.llp.aircasting.util.extensions.showBatteryOptimizationHelperDialog
import pl.llp.aircasting.util.isSDKGreaterOrEqualToM
import javax.inject.Inject

class DashboardFragment : BaseFragment<DashboardViewMvcImpl, DashboardController>() {

    @Inject
    lateinit var settings: Settings

    @Inject
    @Nullable
    lateinit var sessionsSyncService: SessionsSyncService

    private var mTabPosition: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        view = initView(inflater, container)
        controller = DashboardController(view, sessionsSyncService)

        val tabId = arguments?.get("tabId") as Int?
        controller?.onCreate(tabId)

        handleBackButtonPress()

        return view?.rootView
    }

    private fun initView(inflater: LayoutInflater, container: ViewGroup?): DashboardViewMvcImpl? {
        val pagerAdapter = context?.let { DashboardPagerAdapter(it, childFragmentManager) }
        return pagerAdapter?.let {
            DashboardViewMvcImpl(
                requireActivity(),
                inflater, container, childFragmentManager,
                it, DashboardPagerAdapter.TABS_COUNT
            )
        }
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
                it.adjustMenuVisibility(true, settings.followedSessionsCount())
            else it.adjustMenuVisibility(false)
        }
    }

    private fun handleBackButtonPress() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            if (isEnabled && mTabPosition == 0) {
                isEnabled = false
                showBatteryOptimizationDialogIfNeeded()
            }
        }
    }

    private fun showBatteryOptimizationDialogIfNeeded() {
        if (isSDKGreaterOrEqualToM() && !isIgnoringBatteryOptimizations(requireContext()))
            requireActivity().showBatteryOptimizationHelperDialog()
    }
}
