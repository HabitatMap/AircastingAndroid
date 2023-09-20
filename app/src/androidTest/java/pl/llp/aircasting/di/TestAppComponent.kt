package pl.llp.aircasting.di

import dagger.Component
import dagger.Subcomponent
import pl.llp.aircasting.CreateAccountTest
import pl.llp.aircasting.FixedSessionTest
import pl.llp.aircasting.LoginTest
import pl.llp.aircasting.MobileSessionTest
import pl.llp.aircasting.MyAccountTest
import pl.llp.aircasting.OnboardingTest
import pl.llp.aircasting.SearchFollowTest
import pl.llp.aircasting.di.components.AppComponent
import pl.llp.aircasting.di.modules.ApiModule
import pl.llp.aircasting.di.modules.AppModule
import pl.llp.aircasting.di.modules.CoroutineModule
import pl.llp.aircasting.di.modules.FlowModule
import pl.llp.aircasting.di.modules.FragmentModule
import pl.llp.aircasting.di.modules.RepositoryModule
import pl.llp.aircasting.di.modules.SyncModule
import pl.llp.aircasting.di.modules.ViewModelModule
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
    fun inject(test: OnboardingTest)
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
        SyncModule::class,
        FlowModule::class,
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
    fun inject(test: SearchFollowTest)
}