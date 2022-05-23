package pl.llp.aircasting.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.repository.MeasurementStreamsRepository
import pl.llp.aircasting.data.api.repository.MeasurementsRepository
import pl.llp.aircasting.data.api.repository.SessionsRepository
import pl.llp.aircasting.util.exceptions.ErrorHandler
import javax.inject.Singleton

@Module
class AppModule(private val app: AircastingApplication) {

    @Provides
    @Singleton
    fun provideContext(): Context = app.applicationContext

    @Provides
    @Singleton
    fun providesApp(): AircastingApplication = app

//    @Provides
//    @Singleton
//    fun provideAdapter(): FixedFollowAdapter = FixedFollowAdapter(arrayListOf())

    @Provides
    @Singleton
    fun providesErrorHandler(): ErrorHandler = ErrorHandler(app)

    @Provides
    @Singleton
    fun providesSessionsRepository(): SessionsRepository = SessionsRepository()

    @Provides
    @Singleton
    fun providesMeasurementStreamsRepository(): MeasurementStreamsRepository =
        MeasurementStreamsRepository()

    @Provides
    @Singleton
    fun providesMeasurementsRepository(): MeasurementsRepository = MeasurementsRepository()
}
