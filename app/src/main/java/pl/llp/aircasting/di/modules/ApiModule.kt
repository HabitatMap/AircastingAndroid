package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.api.services.Authenticated
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.util.Settings

open class WebServerFactory

@Module
open class ApiModule {
    @Provides
    @UserSessionScope
    @Authenticated
    fun providesApiServiceAuthenticatedWithToken(
        settings: Settings,
        factory: ApiServiceFactory
    ): ApiService {
        return factory.getAuthenticated(settings.getAuthToken())
    }
}
