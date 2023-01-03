package pl.llp.aircasting.ui.view.screens.dashboard.active

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.observers.ActiveSessionsObserver
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsController
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsViewMvc
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import pl.llp.aircasting.util.Settings
import pl.llp.aircasting.util.events.NewMeasurementEvent
import pl.llp.aircasting.util.extensions.expandedCards
import pl.llp.aircasting.util.extensions.safeRegister
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager
import pl.llp.aircasting.util.helpers.sensor.AirBeamReconnector

class MobileActiveController(
    private val mRootActivity: FragmentActivity?,
    private val mViewMvc: SessionsViewMvc?,
    private val mSessionsViewModel: SessionsViewModel,
    mLifecycleOwner: LifecycleOwner,
    private val mSettings: Settings,
    mApiServiceFactory: ApiServiceFactory,
    private val airBeamReconnector: AirBeamReconnector,
    private val permissionsManager: PermissionsManager,
    private val mContext: Context
) : SessionsController(
    mRootActivity,
    mViewMvc,
    mSessionsViewModel,
    mSettings,
    mApiServiceFactory,
    mRootActivity!!.supportFragmentManager,
    mContext
),
    SessionsViewMvc.Listener {

    private var mSessionsObserver =
        ActiveSessionsObserver(mLifecycleOwner, mSessionsViewModel, mViewMvc)

    override fun onCreate() {
        super.onCreate()
    }

    override fun registerSessionsObserver() {
        mSessionsObserver.observe(mSessionsViewModel.loadMobileActiveSessionsWithMeasurements())
    }

    override fun unregisterSessionsObserver() {
        mSessionsObserver.stop()
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().safeRegister(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    override fun onRecordNewSessionClicked() {
        startNewSession(Session.Type.MOBILE)
    }

    override fun onExploreNewSessionsClicked() {
        // Do nothing
    }

    override fun onExpandSessionCard(session: Session) {
        super.onExpandSessionCard(session)
        expandedCards()?.add(session.uuid)
    }

    override fun onCollapseSessionCard(session: Session) {
        expandedCards()?.remove(session.uuid)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NewMeasurementEvent) {
        val deviceId = event.deviceId ?: return

        mViewMvc?.hideLoaderFor(deviceId)
    }
}
