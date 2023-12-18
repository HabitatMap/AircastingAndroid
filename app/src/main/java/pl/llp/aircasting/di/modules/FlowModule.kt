package pl.llp.aircasting.di.modules

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import pl.llp.aircasting.data.model.AirbeamConnectionStatus
import pl.llp.aircasting.di.UserSessionScope
import javax.inject.Qualifier

@Module
class FlowModule {
    @Provides
    @BatteryLevelFlow
    @UserSessionScope
    fun provideBatteryLevelFlow(): MutableSharedFlow<Int> = MutableSharedFlow()

    @Provides
    @AirbeamConnectionStatusFlow
    @UserSessionScope
    fun provideAirbeamConnectionStatusFlow(): MutableStateFlow<AirbeamConnectionStatus?> = MutableStateFlow(null)

    @Provides
    @SyncActiveFlow
    @UserSessionScope
    fun provideSyncActiveFlow(): MutableSharedFlow<Boolean> = MutableSharedFlow()

}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class BatteryLevelFlow

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class AirbeamConnectionStatusFlow

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class SyncActiveFlow