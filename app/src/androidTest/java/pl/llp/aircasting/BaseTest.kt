package pl.llp.aircasting

import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import pl.llp.aircasting.di.TestAppComponent
import pl.llp.aircasting.di.TestUserDependentComponent

abstract class BaseTest {
    lateinit var testAppComponent: TestAppComponent
    lateinit var testUserDependentComponent: TestUserDependentComponent

    lateinit var server: MockWebServer

    init {
//        testAppComponent = DaggerTestAppComponent.builder()
//            .appModule(AppModule(ApplicationProvider.getApplicationContext()))
//            .build()
//        testUserDependentComponent = testAppComponent.userComponentFactory().create()
    }

    @Before
    open fun setup() {

        server.start()
    }

    protected fun inject(test: BaseTest) {
//        testAppComponent.inject(test)
    }

    protected fun injectUserComponent(test: BaseTest) {
//        testUserDependentComponent.inject(test)
    }

    @After
    open fun cleanup() {
        server.shutdown()
    }
}
