package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.data.local.AppDatabase
import pl.llp.aircasting.data.local.repository.ExtSessionsLocalRepository
import pl.llp.aircasting.data.local.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.local.repository.MeasurementsRepository
import pl.llp.aircasting.data.local.repository.SessionsRepository
import javax.inject.Singleton

@Module
class DatabaseModule() {
    @Provides
    @Singleton
    fun providesSessionsRepository(): SessionsRepository = SessionsRepository()

    @Provides
    @Singleton
    fun providesExtSessionsLocalRepository(): ExtSessionsLocalRepository =
        ExtSessionsLocalRepository()

    @Singleton
    @Provides
    fun provideExtSessionDao(db: AppDatabase) = db.extSession()

    @Provides
    @Singleton
    fun providesMeasurementStreamsRepository(): MeasurementStreamsRepository =
        MeasurementStreamsRepository()

    @Provides
    @Singleton
    fun providesMeasurementsRepository(): MeasurementsRepository = MeasurementsRepository()
}