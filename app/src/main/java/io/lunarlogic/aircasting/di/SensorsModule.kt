package io.lunarlogic.aircasting.di

import dagger.Module
import dagger.Provides
import io.lunarlogic.aircasting.exceptions.ErrorHandler
import io.lunarlogic.aircasting.sensor.airbeam2.AirBeam2Connector
import javax.inject.Singleton

@Module
open class SensorsModule {
    @Provides
    @Singleton
    open fun providesAirbeam2Connector(errorHandler: ErrorHandler): AirBeam2Connector = AirBeam2Connector(errorHandler)
}
