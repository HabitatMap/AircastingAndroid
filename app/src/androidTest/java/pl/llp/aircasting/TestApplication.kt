package pl.llp.aircasting

import pl.llp.aircasting.di.DaggerTestAppComponent
import pl.llp.aircasting.di.TestAppComponent
import pl.llp.aircasting.di.UserDependentComponent
import pl.llp.aircasting.di.components.AppComponent
import pl.llp.aircasting.di.modules.AppModule

class TestApplication : AircastingApplication() {
    override fun initialiseAppComponent(): AppComponent {
        return DaggerTestAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }

    override fun initialiseUserDependentComponent(): UserDependentComponent {
        return (appComponent as TestAppComponent).testUserComponentFactory().create()
    }
}