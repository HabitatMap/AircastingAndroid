package io.lunarlogic.aircasting.helpers

import io.lunarlogic.aircasting.di.mocks.FakeApiServiceFactory
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory

fun getFakeApiServiceFactoryFrom(apiServiceFactory: ApiServiceFactory) : FakeApiServiceFactory {
    return (apiServiceFactory as FakeApiServiceFactory)
}
