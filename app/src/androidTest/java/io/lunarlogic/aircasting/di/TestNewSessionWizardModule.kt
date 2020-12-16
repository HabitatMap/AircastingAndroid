package io.lunarlogic.aircasting.di

import io.lunarlogic.aircasting.di.mocks.FakeSessionDetailsControllerFactory
import io.lunarlogic.aircasting.screens.new_session.session_details.*

class TestNewSessionWizardModule: NewSessionWizardModule() {
    override fun providesSessionDetailsControllerFactory(): SessionDetailsControllerFactory
            =
        FakeSessionDetailsControllerFactory()
}
