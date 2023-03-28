package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.data.local.repository.*
import pl.llp.aircasting.di.UserSessionScope

@Module
class DatabaseModule {
    @Provides
    @UserSessionScope
    fun providesSessionsRepository(): SessionsRepository = SessionsRepository()

    @Provides
    @UserSessionScope
    fun providesMeasurementStreamsRepository(): MeasurementStreamsRepository =
        MeasurementStreamsRepository()

    @Provides
    @UserSessionScope
    fun providesMeasurementsRepository(): MeasurementsRepositoryImpl = MeasurementsRepositoryImpl()

    @Provides
    @UserSessionScope
    fun providesActiveSessionRepository(): ActiveSessionMeasurementsRepository = ActiveSessionMeasurementsRepository()

    @Provides
    @UserSessionScope
    fun providesThresholdsRepository(): ThresholdsRepository = ThresholdsRepository()
}