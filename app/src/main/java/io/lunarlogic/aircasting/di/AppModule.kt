package io.lunarlogic.aircasting.di

import dagger.Module
import dagger.Provides
import io.lunarlogic.aircasting.AircastingApplication
import javax.inject.Singleton

@Module
class AppModule(private val app: AircastingApplication) {
    @Provides
    @Singleton
    fun provideApp(): AircastingApplication = app
}