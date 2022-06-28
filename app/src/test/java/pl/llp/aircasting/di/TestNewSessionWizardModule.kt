package pl.llp.aircasting.di

import pl.llp.aircasting.di.mocks.FakeSessionDetailsControllerFactory
import pl.llp.aircasting.di.modules.NewSessionWizardModule
import pl.llp.aircasting.ui.view.screens.new_session.session_details.SessionDetailsControllerFactory

class TestNewSessionWizardModule: NewSessionWizardModule() {
    override fun providesSessionDetailsControllerFactory(): SessionDetailsControllerFactory
            =
        FakeSessionDetailsControllerFactory()
}
