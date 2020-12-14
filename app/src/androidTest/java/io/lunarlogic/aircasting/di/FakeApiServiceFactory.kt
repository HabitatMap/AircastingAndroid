package io.lunarlogic.aircasting.di

import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer

class FakeApiServiceFactory(mSettings: Settings, val mockWebServer: MockWebServer) : ApiServiceFactory(mSettings) {

    override fun baseUrl(): HttpUrl {
        return mockWebServer.url("/")
    }

}

class FakeApiServiceFactoryModule(val mockWebServer: MockWebServer) : ApiModule(){
    override fun providesApiServiceFactory(settings: Settings): ApiServiceFactory
                            = FakeApiServiceFactory(settings, mockWebServer)
}
