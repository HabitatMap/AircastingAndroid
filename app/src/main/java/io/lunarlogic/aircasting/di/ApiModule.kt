package io.lunarlogic.aircasting.di

import dagger.Module
import dagger.Provides
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import javax.inject.Singleton

open class WebServerFactory

@Module
open class ApiModule {
    @Provides
    @Singleton
    open fun providesMockWebServerFactory(): WebServerFactory = WebServerFactory()

    @Provides
    @Singleton
    open fun providesApiServiceFactory(settings: Settings, webServerFactory: WebServerFactory): ApiServiceFactory = ApiServiceFactory(settings)
}
