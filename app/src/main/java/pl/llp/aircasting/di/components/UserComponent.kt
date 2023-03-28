package pl.llp.aircasting.di.components

import dagger.Subcomponent
import pl.llp.aircasting.di.modules.ApiModule
import javax.inject.Scope

@UserSessionScope
@Subcomponent(modules = [ApiModule::class])
interface UserComponent {

    fun inject(target: YourTargetClass) // This could be an Activity, Fragment, or any other class that needs injection

    @Subcomponent.Factory
    interface Factory {
        fun create(): UserComponent
    }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class UserSessionScope
