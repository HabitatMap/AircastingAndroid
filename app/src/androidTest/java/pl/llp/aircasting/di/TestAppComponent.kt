package pl.llp.aircasting.di

import dagger.Component
import dagger.Subcomponent
import pl.llp.aircasting.*
import pl.llp.aircasting.di.components.AppComponent
import pl.llp.aircasting.di.modules.*
import javax.inject.Singleton
@Singleton
@Component(
    modules = [
        AppModule::class,
        TestApiServiceFactoryModule::class,
        TestSettingsModule::class,
        TestDatabaseModule::class,
        TestServerModule::class,
    ]
)
interface TestAppComponent : AppComponent {
    fun testUserComponentFactory(): TestUserDependentComponent.Factory

    fun inject(test: CreateAccountTest)
    fun inject(test: LoginTest)
}

@UserSessionScope
@Subcomponent(
    modules = [
        TestDevicesModule::class,
        ApiModule::class,
        TestNewSessionWizardModule::class,
        ViewModelModule::class,
        CoroutineModule::class,
        FragmentModule::class,
        RepositoryModule::class,
        TestPermissionsModule::class,
    ]
)

interface TestUserDependentComponent : UserDependentComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): TestUserDependentComponent
    }
    fun inject(test: FixedSessionTest)
    fun inject(test: MobileSessionTest)
    fun inject(test: MyAccountTest)
}