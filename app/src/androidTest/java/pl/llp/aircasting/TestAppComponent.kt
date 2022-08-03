package pl.llp.aircasting

import dagger.Component
import pl.llp.aircasting.di.AppComponent
import pl.llp.aircasting.di.modules.*
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        ApiModule::class,
        NetworkModule::class,
        SettingsModule::class,
        PermissionsModule::class,
        SensorsModule::class,
        NewSessionWizardModule::class,
        ViewModelModule::class,
        DatabaseModule::class,
        DispatcherModule::class,
        FragmentModule::class
    ]
)
interface TestAppComponent: AppComponent {
    fun inject(test: LoginTest)
    fun inject(test: CreateAccountTest)
    fun inject(test: MobileSessionTest)
    fun inject(test: FixedSessionTest)
    fun inject(test: MyAccountTest)
    fun inject(test: OnboardingTest)
    fun inject(test: SearchFollowTest)
}
