package pl.llp.aircasting.di

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
        ViewModelModule::class
    ]
)
interface TestAppComponent: AppComponent {
    //fun inject(test: )
}
