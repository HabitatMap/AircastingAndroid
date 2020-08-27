package io.lunarlogic.aircasting.di

import dagger.Module
import dagger.Provides
import io.lunarlogic.aircasting.screens.new_session.session_details.SessionDetailsControllerFactory
import javax.inject.Singleton

@Module
open class NewSessionWizardModule {
    @Provides
    @Singleton
    open fun providesSessionDetailsControllerFactory(): SessionDetailsControllerFactory
            = SessionDetailsControllerFactory()
}
