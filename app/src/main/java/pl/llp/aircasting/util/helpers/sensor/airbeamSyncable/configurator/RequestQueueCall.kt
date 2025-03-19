package pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ble.RequestQueue
import pl.llp.aircasting.di.modules.IoDispatcher
import javax.inject.Inject

fun interface RequestQueueCall {
    suspend operator fun invoke(queue: RequestQueue)

    class Await @Inject constructor(
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : RequestQueueCall {
        override suspend fun invoke(queue: RequestQueue) = withContext(ioDispatcher) {
            queue.await()
        }
    }
}
