package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.ui.view.screens.new_session.session_details.SessionDetailsControllerFactory
import javax.inject.Singleton

@Module
open class NewSessionWizardModule {
    @Provides
    @Singleton
    open fun providesSessionDetailsControllerFactory(): SessionDetailsControllerFactory
            = SessionDetailsControllerFactory()
}