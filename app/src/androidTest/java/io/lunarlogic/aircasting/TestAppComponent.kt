package io.lunarlogic.aircasting

import dagger.Component
import io.lunarlogic.aircasting.di.*
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        ApiModule::class,
        SettingsModule::class,
        PermissionsModule::class,
        SensorsModule::class,
        NewSessionWizardModule::class
    ]
)
interface TestAppComponent: AppComponent {
    fun inject(test: LoginTest)
    fun inject(test: CreateAccountTest)
    fun inject(test: MobileSessionTest)
    fun inject(test: FixedSessionTest)
    fun inject(test: MyAccountTest)
    fun inject(test: OnboardingTest)
}
