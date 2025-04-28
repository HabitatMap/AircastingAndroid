package pl.llp.aircasting.di.mocks

import pl.llp.aircasting.ui.view.screens.new_session.session_details.FixedSessionDetailsViewMvc
import pl.llp.aircasting.ui.view.screens.new_session.session_details.Network
import pl.llp.aircasting.ui.view.screens.new_session.session_details.SessionDetailsController

class FakeFixedSessionDetailsController(
    mViewMvc: FixedSessionDetailsViewMvc?
): SessionDetailsController(mViewMvc) {
    companion object {
        val TEST_WIFI_SSID = "fake-wifi"
    }
    init {
        mViewMvc?.bindNetworks(listOf(Network(TEST_WIFI_SSID, 777, 2)))
    }
}
