package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import pl.llp.aircasting.data.api.repository.ThresholdAlertRepository
import pl.llp.aircasting.data.api.repository.ThresholdAlertRepositoryDefault
import javax.inject.Singleton

@Module
class RepositoryModule {
    @Provides
    @Singleton
    fun providesThresholdAlertRepository(repository: ThresholdAlertRepositoryDefault)
            : ThresholdAlertRepository = repository
}