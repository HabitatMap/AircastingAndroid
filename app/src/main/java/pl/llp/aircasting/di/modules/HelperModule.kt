package pl.llp.aircasting.di.modules

import dagger.Binds
import dagger.Module
import pl.llp.aircasting.data.api.services.FixedSessionUploader
import pl.llp.aircasting.data.api.services.FixedSessionUploaderDefault
import pl.llp.aircasting.util.helpers.sensor.common.SessionFinisher
import pl.llp.aircasting.util.helpers.sensor.common.SessionFinisherDefault

@Suppress("unused")
@Module
abstract class HelperModule {
    @Binds
    internal abstract fun bindSessionFinisher(impl: SessionFinisherDefault): SessionFinisher

    @Binds
    internal abstract fun bindFixedSessionUploader(impl: FixedSessionUploaderDefault): FixedSessionUploader
}