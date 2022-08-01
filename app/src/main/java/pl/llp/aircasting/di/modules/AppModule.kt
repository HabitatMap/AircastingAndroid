package pl.llp.aircasting.di.modules

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import dagger.Module
import dagger.Provides
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.di.factories.FragmentsFactory
import pl.llp.aircasting.util.exceptions.ErrorHandler
import javax.inject.Provider
import javax.inject.Singleton

@Module
class AppModule(private val app: AircastingApplication) {

    @Provides
    @Singleton
    fun provideContext(): Context = app.applicationContext

    @Provides
    @Singleton
    fun providesApp(): AircastingApplication = app

    @Provides
    @Singleton
    fun providesErrorHandler(): ErrorHandler = ErrorHandler(app)

    @Provides
    fun provideFragmentFactory(
        fragmentProviders: Map<Class<out Fragment>, @JvmSuppressWildcards Provider<Fragment>>
    ): FragmentFactory {
        return FragmentsFactory(fragmentProviders)
    }
}
