package pl.llp.aircasting

import androidx.test.core.app.ApplicationProvider
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import pl.llp.aircasting.di.TestAppComponent
import pl.llp.aircasting.di.TestUserDependentComponent

abstract class BaseTest {
    lateinit var server: MockWebServer

    @Before
    open fun setup() {
        server.start()
    }

    protected val userComponent
        get(): TestUserDependentComponent? =
            with(ApplicationProvider.getApplicationContext() as TestApplication)
            {
                onUserLoggedIn()
                (userDependentComponent as? TestUserDependentComponent)
            }
    protected val appComponent
        get(): TestAppComponent =
            with(ApplicationProvider.getApplicationContext() as TestApplication)
            {
                (appComponent as TestAppComponent)
            }

    @After
    open fun cleanup() {
        server.shutdown()
    }
}
