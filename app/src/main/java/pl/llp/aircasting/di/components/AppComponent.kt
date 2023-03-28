package pl.llp.aircasting.di.components

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.api.services.Authenticated
import pl.llp.aircasting.data.api.services.NonAuthenticated
import pl.llp.aircasting.di.UserDependentComponent
import pl.llp.aircasting.di.modules.ApiModule
import pl.llp.aircasting.di.modules.WebServerFactory
import pl.llp.aircasting.util.Settings
import javax.inject.Scope
import javax.inject.Singleton

@Singleton
@Subcomponent(modules = [NonAuthenticatedApiModule::class])
interface AppComponent {

    fun inject(target: YourTargetClass) // This could be an Activity, Fragment, or any other class that needs injection

    fun userComponentFactory(): UserDependentComponent.Factory
}

@Module
open class NonAuthenticatedApiModule {
    @Provides
    @NonAuthenticated
    fun provideApiServiceNonAuthenticated(factory: ApiServiceFactory): ApiService {
        return factory.getNonAuthenticated()
    }
}

