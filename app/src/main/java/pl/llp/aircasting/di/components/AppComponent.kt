package pl.llp.aircasting.di.components

import dagger.Component
import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.api.services.NonAuthenticated
import pl.llp.aircasting.di.UserDependentComponent
import pl.llp.aircasting.di.modules.AppModule
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.ui.view.screens.create_account.CreateAccountActivity
import pl.llp.aircasting.ui.view.screens.login.LoginActivity
import pl.llp.aircasting.util.Settings
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        NonAuthenticatedModule::class
    ]
)
interface AppComponent {

    fun inject(target: AircastingApplication)
    fun inject(target: CreateAccountActivity)
    fun inject(target: LoginActivity)
    fun inject(target: BaseActivity)

    fun userComponentFactory(): UserDependentComponent.Factory
}

@Module
open class NonAuthenticatedModule {
    @Provides
    @NonAuthenticated
    fun provideApiServiceNonAuthenticated(factory: ApiServiceFactory): ApiService {
        return factory.getNonAuthenticated()
    }

    @Provides
    @Singleton
    open fun providesSettings(application: AircastingApplication): Settings = application.settings
}

