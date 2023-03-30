package pl.llp.aircasting

import dagger.Component
import dagger.Subcomponent
import pl.llp.aircasting.di.TestApiModule
import pl.llp.aircasting.di.TestDatabaseModule
import pl.llp.aircasting.di.TestPermissionsModule
import pl.llp.aircasting.di.UserSessionScope
import pl.llp.aircasting.di.components.NonAuthenticatedModule
import pl.llp.aircasting.di.modules.*
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        TestApiModule::class,
        NonAuthenticatedModule::class,
        TestPermissionsModule::class,
        TestDatabaseModule::class,
    ]
)
interface TestAppComponent {
    fun userComponentFactory(): TestUserDependentComponent.Factory
    fun inject(test: LoginTest)
    fun inject(test: CreateAccountTest)
    fun inject(test: MobileSessionTest)
    fun inject(test: FixedSessionTest)
    fun inject(test: MyAccountTest)
    fun inject(test: OnboardingTest)
    fun inject(test: SearchFollowTest)
}

@UserSessionScope
@Subcomponent(
    modules = [
        SensorsModule::class,
        NewSessionWizardModule::class,
        ViewModelModule::class,
        CoroutineModule::class,
        FragmentModule::class,
        RepositoryModule::class
    ]
)
interface TestUserDependentComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): TestUserDependentComponent
    }
    fun inject(test: LoginTest)
    fun inject(test: CreateAccountTest)
    fun inject(test: MobileSessionTest)
    fun inject(test: FixedSessionTest)
    fun inject(test: MyAccountTest)
    fun inject(test: OnboardingTest)
    fun inject(test: SearchFollowTest)
}