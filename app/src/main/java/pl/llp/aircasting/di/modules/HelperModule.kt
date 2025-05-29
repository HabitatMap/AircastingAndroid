package pl.llp.aircasting.di.modules

import dagger.Binds
import dagger.Module
import pl.llp.aircasting.util.helpers.sensor.common.DefaultSessionFinisher
import pl.llp.aircasting.util.helpers.sensor.common.SessionFinisher

@Suppress("unused")
@Module
abstract class HelperModule {
    @Binds
    internal abstract fun bindSessionFinisher(impl: DefaultSessionFinisher): SessionFinisher
}