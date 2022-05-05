package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.local.repositories.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repositories.MeasurementsRepository
import pl.llp.aircasting.data.local.repositories.SessionsRepository
import pl.llp.aircasting.util.exceptions.ErrorHandler
import javax.inject.Singleton

@Module
class AppModule(private val app: AircastingApplication) {
    @Provides
    @Singleton
    fun providesApp(): AircastingApplication = app

    @Provides
    @Singleton
    fun providesErrorHandler(): ErrorHandler = ErrorHandler(app)

    @Provides
    @Singleton
    fun providesSessionsRepository(): SessionsRepository = SessionsRepository()

    @Provides
    @Singleton
    fun providesMeasurementStreamsRepository(): MeasurementStreamsRepository = MeasurementStreamsRepository()

    @Provides
    @Singleton
    fun providesMeasurementsRepository(): MeasurementsRepository = MeasurementsRepository()
}
