package pl.llp.aircasting.di

import dagger.Component
import dagger.Subcomponent
import pl.llp.aircasting.di.components.AppComponent
import pl.llp.aircasting.di.modules.ApiModule
import pl.llp.aircasting.di.modules.FlowModule
import pl.llp.aircasting.di.modules.FragmentModule
import pl.llp.aircasting.di.modules.RepositoryModule
import pl.llp.aircasting.di.modules.ViewModelModule
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.sync.SDCardSyncServiceIntegratedTest
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        TestAppModule::class,
        TestApiServiceFactoryModule::class,
        TestSettingsModule::class,
        TestDatabaseModule::class,
        TestServerModule::class,
    ]
)
interface TestAppComponent : AppComponent {
    fun testUserComponentFactory(): TestUserDependentComponent.Factory
}

@UserSessionScope
@Subcomponent(
    modules = [
        TestDevicesModule::class,
        ApiModule::class,
        TestNewSessionWizardModule::class,
        ViewModelModule::class,
        TestCoroutineModule::class,
        FragmentModule::class,
        RepositoryModule::class,
        TestPermissionsModule::class,
        TestSyncModule::class,
        FlowModule::class,
    ]
)

interface TestUserDependentComponent : UserDependentComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): TestUserDependentComponent
    }
    fun inject(test: SDCardSyncServiceIntegratedTest)
}