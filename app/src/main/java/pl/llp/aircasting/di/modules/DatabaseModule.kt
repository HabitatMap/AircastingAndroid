package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.data.local.repository.*
import javax.inject.Singleton

@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun providesSessionsRepository(): SessionsRepository = SessionsRepository()

    @Provides
    @Singleton
    fun providesMeasurementStreamsRepository(): MeasurementStreamsRepository =
        MeasurementStreamsRepository()

    @Provides
    @Singleton
    fun providesMeasurementsRepository(): MeasurementsRepositoryImpl = MeasurementsRepositoryImpl()

    @Provides
    @Singleton
    fun providesActiveSessionRepository(): ActiveSessionMeasurementsRepository = ActiveSessionMeasurementsRepository()

    @Provides
    @Singleton
    fun providesThresholdsRepository(): ThresholdsRepository = ThresholdsRepository()
}