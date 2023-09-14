package pl.llp.aircasting.di

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.di.mocks.FakeSessionDetailsControllerFactory
import pl.llp.aircasting.ui.view.screens.new_session.session_details.SessionDetailsControllerFactory

@Module
class TestNewSessionWizardModule {
    @Provides
    @UserSessionScope
    fun providesSessionDetailsControllerFactory(): SessionDetailsControllerFactory =
        FakeSessionDetailsControllerFactory()
}
