package pl.llp.aircasting.di

import android.app.Application
import dagger.Module
import dagger.Provides
import okhttp3.mockwebserver.MockWebServer
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.interceptor.NetworkConnectionInterceptor
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.di.mocks.FakeApiServiceFactory
import pl.llp.aircasting.di.mocks.FakeSettings
import pl.llp.aircasting.di.modules.NonAuthenticatedModule
import pl.llp.aircasting.util.Settings
import javax.inject.Singleton

@Module(includes = [NonAuthenticatedModule::class])
class TestSettingsModule {
    @Provides
    @Singleton
    fun providesSettings(application: AircastingApplication): Settings = FakeSettings(
        application.getSharedPreferences(
            Settings.PREFERENCES_NAME,
            Application.MODE_PRIVATE
        )
    )
}

@Module(includes = [NonAuthenticatedModule::class])
class TestApiServiceFactoryModule {
    @Provides
    @Singleton
    fun providesApiServiceFactory(
        server: MockWebServer,
        settings: Settings,
        networkConnectionInterceptor: NetworkConnectionInterceptor,
    ): ApiServiceFactory =
        FakeApiServiceFactory(server, settings, networkConnectionInterceptor)
}
