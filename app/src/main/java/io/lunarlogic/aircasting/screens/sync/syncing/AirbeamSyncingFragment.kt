package io.lunarlogic.aircasting.screens.sync.syncing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.screens.common.BaseWizardNavigator

class AirbeamSyncingFragment(
    private val mFragmentManager: FragmentManager
): Fragment(), BaseWizardNavigator.BackPressedListener {
    private var controller: AirbeamSyncingController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .appComponent.inject(this)

        val view = AirbeamSyncingViewMvcImpl(layoutInflater, null)
        controller = AirbeamSyncingController(mFragmentManager, view)

        controller?.onCreate()

        return view.rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        controller?.onDestroy()
    }

    override fun onBackPressed() {
        controller?.onBackPressed()
    }
}
