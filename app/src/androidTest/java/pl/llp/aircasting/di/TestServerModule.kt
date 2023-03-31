package pl.llp.aircasting.di

import dagger.Module
import dagger.Provides
import okhttp3.mockwebserver.MockWebServer
import javax.inject.Singleton

@Module
class TestServerModule {
    @Provides
    @Singleton
    fun providesServer(): MockWebServer = MockWebServer()
}
