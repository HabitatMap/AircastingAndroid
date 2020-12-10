package io.lunarlogic.aircasting.di

import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.networking.services.ApiServiceFactory
import okhttp3.HttpUrl

class FakeApiServiceFactory(mSettings: Settings) : ApiServiceFactory(mSettings) {

    override fun baseUrl(): HttpUrl {
        return HttpUrl.get("/")
    }

}
