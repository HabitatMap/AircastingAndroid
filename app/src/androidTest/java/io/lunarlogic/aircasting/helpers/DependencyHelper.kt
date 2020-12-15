package io.lunarlogic.aircasting.helpers

import io.lunarlogic.aircasting.di.FakeApiServiceFactory
import io.lunarlogic.aircasting.di.MockWebServerFactory
import io.lunarlogic.aircasting.di.TestMockWebServerFactory
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory

fun FakeApiServiceFactoryConversion(apiFactory: ApiServiceFactory) : FakeApiServiceFactory{
    return (apiFactory as FakeApiServiceFactory)
}

fun TestMockWebServerFactoryConversion(mockWebServerFactory: MockWebServerFactory) : TestMockWebServerFactory{
    return (mockWebServerFactory as TestMockWebServerFactory)
}
