package pl.llp.aircasting

import android.content.Intent
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.di.TestApiModule
import pl.llp.aircasting.di.TestSettingsModule
import pl.llp.aircasting.di.modules.AppModule
import pl.llp.aircasting.di.modules.PermissionsModule
import pl.llp.aircasting.helpers.clickOnFirstItem
import pl.llp.aircasting.helpers.getMockWebServerFrom
import pl.llp.aircasting.helpers.hintContainsString
import pl.llp.aircasting.helpers.waitAndRetry
import pl.llp.aircasting.ui.view.fragments.search_follow_fixed_session.MapResultFragment
import pl.llp.aircasting.ui.view.fragments.search_follow_fixed_session.SearchLocationFragment
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.util.Settings
import javax.inject.Inject

/*
* When running test suite, do not forget to disable animations on the test device
* Otherwise some test may fail
* */

@RunWith(AndroidJUnit4::class)
class SearchFollowTest {
    companion object {
        private lateinit var startIntent: Intent
        @BeforeClass
        @JvmStatic
        fun setupIntent() {
            startIntent =
                Intent(
                    ApplicationProvider.getApplicationContext<AircastingApplication>(),
                    MainActivity::class.java
                )
        }
    }

    @Inject
    lateinit var apiServiceFactory: ApiServiceFactory
    @Inject
    lateinit var settings: Settings
    lateinit var activityScenario: ActivityScenario<MainActivity>
    lateinit var searchScenario: FragmentScenario<SearchLocationFragment>
    lateinit var mapScenario: FragmentScenario<MapResultFragment>
    lateinit var server: MockWebServer

    private val newYork = "New York"
    private val losAngeles = "Los Angeles"

    @Before
    fun setup() {
        setupDagger()

        server = getMockWebServerFrom(apiServiceFactory)
        server.start()

        settings.login("NAME", "EMAIL", "TOKEN")
    }

    private fun setupDagger() {
        val app = ApplicationProvider.getApplicationContext<AircastingApplication>()
        val permissionsModule = PermissionsModule()
        val testAppComponent = DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .apiModule(TestApiModule())
            .settingsModule(TestSettingsModule())
            .permissionsModule(permissionsModule)
            .build()
        app.appComponent = testAppComponent
        testAppComponent.inject(this)
    }

    @After
    fun cleanup() {
        server.shutdown()
    }

    @Test
    fun whenClickingSearchButton_goesToSearchFixedSessionsScreen() {
        activityScenario = ActivityScenario.launch(startIntent)

        onView(withId(R.id.search_follow_icon))
            .perform(click())

        onView(withId(R.id.places_autocomplete_search_input))
            .check(matches(isDisplayed()))
        onView(withId(R.id.chipGroupFirstLevel))
            .check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun whenChangingParameter_secondChipGroupIsDisplayed() {
        launchSearchScreen()

        onView(withId(R.id.ozone_chip))
            .perform(click())

        onView(withId(R.id.chipGroupSecondLevelTwo))
            .check(matches(isDisplayed()))

        searchScenario.close()
    }

    @Test
    fun whenChoosingPlace_itsNameIsDisplayedInTextView() {
        launchSearchScreen()

        searchForPlace(newYork)

        searchFieldHasHint(newYork)

        searchScenario.close()
    }

    private fun searchForPlace(place: String) {
        onView(withId(R.id.places_autocomplete_search_input))
            .perform(click())

        onView(withId(R.id.places_autocomplete_search_bar))
            .perform(typeText(place))
        onView(withId(R.id.places_autocomplete_search_bar))
            .check(matches(withText(place)))

        waitAndRetry {
            onView(withId(R.id.places_autocomplete_list))
                .perform(clickOnFirstItem())
        }
    }

    private fun searchFieldHasHint(hint: String) {
        onView(withId(R.id.places_autocomplete_search_input))
            .check(matches(hintContainsString(hint)))
    }

    private fun launchSearchScreen() {
        searchScenario = launchFragmentInContainer(themeResId = R.style.Theme_Aircasting)
    }
}