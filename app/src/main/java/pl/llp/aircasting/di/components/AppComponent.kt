package pl.llp.aircasting.di.components

import dagger.Component
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.LauncherActivity
import pl.llp.aircasting.di.UserDependentComponent
import pl.llp.aircasting.di.modules.ApiServiceFactoryModule
import pl.llp.aircasting.di.modules.AppModule
import pl.llp.aircasting.di.modules.DatabaseModule
import pl.llp.aircasting.di.modules.SettingsModule
import pl.llp.aircasting.ui.view.common.BaseActivity
import pl.llp.aircasting.ui.view.screens.create_account.CreateAccountActivity
import pl.llp.aircasting.ui.view.screens.login.LoginActivity
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        ApiServiceFactoryModule::class,
        SettingsModule::class,
        DatabaseModule::class,
    ]
)
interface AppComponent {

    fun inject(target: AircastingApplication)
    fun inject(target: CreateAccountActivity)
    fun inject(target: LoginActivity)
    fun inject(target: BaseActivity)
    fun inject(target: LauncherActivity)
    fun userComponentFactory(): UserDependentComponent.Factory
}
