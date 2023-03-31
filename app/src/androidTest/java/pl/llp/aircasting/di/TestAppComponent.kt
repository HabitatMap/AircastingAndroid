package pl.llp.aircasting.di

import dagger.Component
import dagger.Subcomponent
import pl.llp.aircasting.BaseTest
import pl.llp.aircasting.CreateAccountTest
import pl.llp.aircasting.di.modules.*
import javax.inject.Singleton
@Singleton
@Component(
    modules = [
        AppModule::class,
        TestApiServiceFactoryModule::class,
        TestSettingsModule::class,
        TestDatabaseModule::class,
        ServerModule::class,
    ]
)
interface TestAppComponent {
    fun userComponentFactory(): TestUserDependentComponent.Factory

    fun inject(test: BaseTest)
//    fun inject(test: LoginTest)
    fun inject(test: CreateAccountTest)
//    fun inject(test: MobileSessionTest)
//    fun inject(test: FixedSessionTest)
//    fun inject(test: MyAccountTest)
//    fun inject(test: OnboardingTest)
//    fun inject(test: SearchFollowTest)
}

@UserSessionScope
@Subcomponent(
    modules = [
        TestDevicesModule::class,
        ApiModule::class,
        NewSessionWizardModule::class,
        ViewModelModule::class,
        CoroutineModule::class,
        FragmentModule::class,
        RepositoryModule::class,
        TestPermissionsModule::class,

//        PermissionsModule::class,
    ]
)
interface TestUserDependentComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): TestUserDependentComponent
    }
    fun inject(test: BaseTest)
}