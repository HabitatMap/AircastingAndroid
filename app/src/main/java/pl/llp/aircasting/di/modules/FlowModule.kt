package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.flow.MutableSharedFlow
import pl.llp.aircasting.di.UserSessionScope
import javax.inject.Qualifier

@Module
class FlowModule {
    @Provides
    @BatteryLevelFlow
    @UserSessionScope
    fun provideBatteryLevelFlow(): MutableSharedFlow<Int> = MutableSharedFlow()
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class BatteryLevelFlow