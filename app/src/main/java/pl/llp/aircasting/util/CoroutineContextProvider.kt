package pl.llp.aircasting.util

import kotlin.coroutines.CoroutineContext

interface CoroutineContextProvider {
    fun context(): CoroutineContext
}

class CoroutineContextProviderImpl(
    private val context: CoroutineContext
) : CoroutineContextProvider {

    override fun context(): CoroutineContext = context
}
