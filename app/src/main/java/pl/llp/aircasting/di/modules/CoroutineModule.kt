package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Module
object CoroutineModule {
    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @IoCoroutineScope
    @Provides
    fun provideIoCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO)
    @MainScope
    @Provides
    fun provideMainScope(): CoroutineScope = CoroutineScope(Dispatchers.Main)
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainScope

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoCoroutineScope