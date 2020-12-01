package io.lunarlogic.aircasting

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import io.lunarlogic.aircasting.di.AppModule
import io.lunarlogic.aircasting.di.MockWebServerModule
import io.lunarlogic.aircasting.di.PermissionsModule
import io.lunarlogic.aircasting.di.TestSettingsModule
import io.lunarlogic.aircasting.lib.Settings
import io.lunarlogic.aircasting.screens.settings.myaccount.MyAccountActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class MyAccountTest {

    //TODO: test case'y??

    @Inject
    lateinit var settings: Settings

    @Rule
    val testRule : ActivityTestRule<MyAccountActivity>
            = ActivityTestRule(MyAccountActivity::class.java, false, false)

    private fun setupDagger(){
        // todo: for now its just copied from LoginTest, have to think if its ok
        val app = ApplicationProvider.getApplicationContext<AircastingApplication>()
        val permissionsModule = PermissionsModule()
        val testAppComponent = DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .settingsModule(TestSettingsModule())
            .permissionsModule(permissionsModule)
            .mockWebServerModule(MockWebServerModule())
            .build()
        app.appComponent = testAppComponent
        testAppComponent.inject(this)
    }

    @Before
    fun setup(){
        // todo: anything more here??
        setupDagger()
    }

    @After
    fun cleanup(){

    }

    @Test
    fun MyAccountTest1(){
        //todo: some initializations before activity launch ?
        // i need to have right values in settings here

        testRule.launchActivity(null)

        // checking if text on text view matches what I want:
        onView(withId(R.id.login_state_textView)).check(matches(withText("")))

        //  performing click on button:
        onView(withId(R.id.sign_out_button)).perform(click())

        Thread.sleep(2000)
        // after sleep assertions about: 1) settings values 2) database values 3) launched activity <??>

    }


}