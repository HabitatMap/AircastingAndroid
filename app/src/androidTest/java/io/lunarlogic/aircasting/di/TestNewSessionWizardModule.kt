package io.lunarlogic.aircasting.di

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.lunarlogic.aircasting.screens.new_session.session_details.*
import io.lunarlogic.aircasting.sensor.Session

class FakeFixedSessionDetailsController(
    private val mContext: Context?,
    private val mViewMvc: FixedSessionDetailsViewMvc
): SessionDetailsController(mContext, mViewMvc) {
    companion object {
        val TEST_WIFI_SSID = "fake-wifi"
    }
    init {
        mViewMvc.bindNetworks(listOf(Network(TEST_WIFI_SSID, 777, 2)))
    }
}

class FakeSessionDetailsControllerFactory: SessionDetailsControllerFactory() {
    override fun get(mContextActivity: FragmentActivity?,
                     view: SessionDetailsViewMvc,
                     sessionType: Session.Type,
                     fragmentManager: FragmentManager
    ): SessionDetailsController {
        if (sessionType == Session.Type.FIXED) {
            return FakeFixedSessionDetailsController(mContextActivity, view as FixedSessionDetailsViewMvcImpl)
        }

        return super.get(mContextActivity, view, sessionType, fragmentManager)
    }
}


class TestNewSessionWizardModule: NewSessionWizardModule() {
    override fun providesSessionDetailsControllerFactory(): SessionDetailsControllerFactory
            = FakeSessionDetailsControllerFactory()
}
