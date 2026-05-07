package pl.llp.aircasting.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.ui.view.common.BaseFragment
import pl.llp.aircasting.ui.view.common.BaseWizardNavigator
import pl.llp.aircasting.ui.view.screens.sync.syncing.AirbeamSyncingController
import pl.llp.aircasting.ui.view.screens.sync.syncing.AirbeamSyncingViewMvc
import pl.llp.aircasting.ui.view.screens.sync.syncing.AirbeamSyncingViewMvcImpl
import pl.llp.aircasting.util.exceptions.ErrorHandler
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository
import javax.inject.Inject

class AirbeamSyncingFragment(
    private val mFragmentManager: FragmentManager,
    private val mErrorHandler: ErrorHandler
) : BaseFragment<AirbeamSyncingViewMvcImpl, AirbeamSyncingController>(), BaseWizardNavigator.BackPressedListener {
    var listener: AirbeamSyncingViewMvc.Listener? = null

    @Inject
    lateinit var v2StateRepository: AirBeamMiniV2StateRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity?.application as AircastingApplication)
            .userDependentComponent?.inject(this)

        view = AirbeamSyncingViewMvcImpl(layoutInflater, null)
        controller = AirbeamSyncingController(view, mFragmentManager, mErrorHandler, v2StateRepository)

        controller?.onCreate()

        return view?.rootView
    }

    override fun onStart() {
        super.onStart()
        listener?.let { controller?.registerListener(it) }
    }

    override fun onBackPressed() {
        controller?.onBackPressed()
    }
}
