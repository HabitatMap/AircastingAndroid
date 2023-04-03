package pl.llp.aircasting.ui.view.screens.dashboard

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import pl.llp.aircasting.R
import pl.llp.aircasting.data.api.util.TAG
import pl.llp.aircasting.ui.view.common.BaseObservableViewMvc

open class DashboardViewMvcImpl(
    private val mRootActivity: FragmentActivity,
    inflater: LayoutInflater,
    parent: ViewGroup?,
    fragmentManager: FragmentManager?,
    adapter: FragmentPagerAdapter,
    tabsCount: Int
) : DashboardViewMvc, BaseObservableViewMvc<DashboardViewMvc.Listener>() {
    private val mPager: ViewPager?
    private var tabs: TabLayout? = null
    protected var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    init {
        this.rootView = inflater.inflate(R.layout.fragment_dashboard, parent, false)
        tabs = findViewById(R.id.tabs)
        mPager = findViewById(R.id.pager)
        tabs?.setupWithViewPager(mPager)
        mSwipeRefreshLayout = findViewById(R.id.refresh_sessions)
        mPager.offscreenPageLimit = tabsCount
        fragmentManager?.let { mPager.adapter = adapter }
        setTabsMargins()
    }

    fun setup() {
        setupSwipeToRefreshLayout()
    }

    override fun goToTab(tabId: Int) {
        mPager?.currentItem = tabId
    }

    override fun showLoader() {
        Log.d(TAG, "show mSwipeRefreshLayout: $mSwipeRefreshLayout")
        mSwipeRefreshLayout?.isRefreshing = true
    }

    override fun hideLoader() {
        Log.d(TAG, "hide mSwipeRefreshLayout: $mSwipeRefreshLayout")
        mSwipeRefreshLayout?.isRefreshing = false
    }

    private fun setTabsMargins() {
        val firstTab = tabs?.getChildAt(0)
        val firstTabParams = firstTab?.layoutParams as ViewGroup.MarginLayoutParams
        val leftMargin = context.resources.getDimension(R.dimen.navigation_tabs_left_margin).toInt()
        val rightMargin = context.resources.getDimension(R.dimen.keyline_4).toInt()

        firstTabParams.setMargins(leftMargin, 0, rightMargin, 0)
        firstTab.requestLayout()
    }

    protected open fun setupSwipeToRefreshLayout() {
        mSwipeRefreshLayout?.let { layout ->
            layout.setColorSchemeResources(R.color.aircasting_blue_400)
            layout.setOnRefreshListener {
                onSwipeToRefreshTriggered()
            }
        }
    }

    private fun onSwipeToRefreshTriggered() {
        for (listener in listeners) {
            listener.onRefreshTriggered()
        }
    }
}
