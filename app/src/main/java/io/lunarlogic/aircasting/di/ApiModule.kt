package io.lunarlogic.aircasting.di

import dagger.Module
import dagger.Provides
import io.lunarlogic.aircasting.AircastingApplication
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import javax.inject.Singleton

@Module
open class ApiModule {
    @Provides
    @Singleton
    open fun providesApiServiceFactory(settings: Settings): ApiServiceFactory = ApiServiceFactory(settings)
}
