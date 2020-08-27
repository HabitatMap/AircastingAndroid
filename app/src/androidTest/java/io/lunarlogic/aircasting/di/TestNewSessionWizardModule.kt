package io.lunarlogic.aircasting.di

import android.content.Context
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
        mViewMvc.bindNetworks(listOf(Network(TEST_WIFI_SSID, 777)))
    }
}

class FakeSessionDetailsControllerFactory: SessionDetailsControllerFactory() {
    override fun get(context: Context?,
                     view: SessionDetailsViewMvc,
                     sessionType: Session.Type
    ): SessionDetailsController {
        if (sessionType == Session.Type.FIXED) {
            return FakeFixedSessionDetailsController(context, view as FixedSessionDetailsViewMvcImpl)
        }

        return super.get(context, view, sessionType)
    }
}


class TestNewSessionWizardModule: NewSessionWizardModule() {
    override fun providesSessionDetailsControllerFactory(): SessionDetailsControllerFactory
            = FakeSessionDetailsControllerFactory()
}
