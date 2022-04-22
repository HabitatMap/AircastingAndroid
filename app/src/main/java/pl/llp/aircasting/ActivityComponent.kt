package pl.llp.aircasting

import android.app.Activity
import dagger.Component
import pl.llp.aircasting.di.*
import pl.llp.aircasting.di.scopes.ActivityScope

@ActivityScope
@Component(
    modules = [
        ActivityModule::class
    ],
    dependencies = [
        AppComponent::class
    ]
)
interface ActivityComponent {
    fun inject(activity: Activity)
}
