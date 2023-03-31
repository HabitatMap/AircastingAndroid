package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.api.services.NonAuthenticated
import pl.llp.aircasting.util.Settings
import javax.inject.Singleton

@Module
open class NonAuthenticatedModule {
    @Provides
    @NonAuthenticated
    fun provideApiServiceNonAuthenticated(factory: ApiServiceFactory): ApiService {
        return factory.getNonAuthenticated()
    }
}

@Module(includes = [NonAuthenticatedModule::class])
class ApiServiceFactoryModule {
    @Provides
    @Singleton
    fun providesApiServiceFactory(settings: Settings): ApiServiceFactory =
        ApiServiceFactory(settings)
}

@Module(includes = [NonAuthenticatedModule::class])
class SettingsModule {
    @Provides
    @Singleton
    fun providesSettings(application: AircastingApplication): Settings = application.settings
}