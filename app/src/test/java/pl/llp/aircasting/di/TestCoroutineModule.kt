package pl.llp.aircasting.di

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import pl.llp.aircasting.di.modules.IoCoroutineScope
import pl.llp.aircasting.di.modules.IoDispatcher
import pl.llp.aircasting.di.modules.MainDispatcher
import pl.llp.aircasting.di.modules.MainScope

@OptIn(ExperimentalCoroutinesApi::class)
@Module
object TestCoroutineModule {
    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = StandardTestDispatcher()

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = StandardTestDispatcher()

    @IoCoroutineScope
    @Provides
    fun provideIoCoroutineScope(): CoroutineScope = TestScope()
    @MainScope
    @Provides
    fun provideMainScope(): CoroutineScope = TestScope()
}