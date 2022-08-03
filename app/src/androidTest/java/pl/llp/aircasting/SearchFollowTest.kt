package pl.llp.aircasting

import android.content.Intent
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentFactory
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
import com.google.android.material.chip.Chip
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.isA
import org.hamcrest.core.AllOf.allOf
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.api.util.StringConstants.airbeam
import pl.llp.aircasting.data.api.util.StringConstants.measurementTypeOzone
import pl.llp.aircasting.data.api.util.StringConstants.measurementTypePM
import pl.llp.aircasting.data.api.util.StringConstants.openAQ
import pl.llp.aircasting.data.api.util.StringConstants.purpleAir
import pl.llp.aircasting.di.TestApiModule
import pl.llp.aircasting.di.TestSettingsModule
import pl.llp.aircasting.di.modules.AppModule
import pl.llp.aircasting.di.modules.PermissionsModule
import pl.llp.aircasting.helpers.*
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
    @Inject
    lateinit var fragmentFactory: FragmentFactory
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

    @Test
    fun whenTappingContinue_goesToMapScreen_withCorrectInputParameters() {
        activityScenario = ActivityScenario.launch(startIntent)
        onView(withId(R.id.search_follow_icon))
            .perform(click())

        searchAndValidateDisplayedParameters(newYork, measurementTypePM, airbeam)
        searchAndValidateDisplayedParameters(newYork, measurementTypePM, openAQ)
        searchAndValidateDisplayedParameters(newYork, measurementTypePM, purpleAir)
        searchAndValidateDisplayedParameters(newYork, measurementTypeOzone, openAQ)

        searchAndValidateDisplayedParameters(losAngeles, measurementTypePM, airbeam)
        searchAndValidateDisplayedParameters(losAngeles, measurementTypePM, openAQ)
        searchAndValidateDisplayedParameters(losAngeles, measurementTypePM, purpleAir)
        searchAndValidateDisplayedParameters(losAngeles, measurementTypeOzone, openAQ)

        activityScenario.close()
    }

    @Test
    fun mapScreen() {
        launchMapScreen()
        Thread.sleep(5000)
        mapScenario.close()
    }

    private fun searchAndValidateDisplayedParameters(
        place: String,
        parameter: String,
        sensor: String
    ) {
        searchForPlace(place)
        selectSensor(parameter, sensor)

        goToMapScreen()

        searchFieldHasHint(place)
        displayedMeasurementTypeMatches(parameter)
        displayedSensorNameMatches(sensor)

        goBack()
    }

    private fun displayedSensorNameMatches(sensor: String) {
        onView(withId(R.id.txtUsing))
            .check(matches(textContainsString(sensor)))
    }

    private fun goToMapScreen() {
        onView(withId(R.id.btnContinue)).perform(click())
    }

    private fun displayedMeasurementTypeMatches(type: String) {
        onView(withId(R.id.txtShowing))
            .check(matches(textContainsString(type)))
    }


    private fun selectSensor(parameter: String, sensor: String) {
        onView(
            allOf(
                isA(Chip::class.java),
                withParent(withId(R.id.chipGroupFirstLevel)),
                textContainsString(parameter)
            )
        )
            .perform(click())

        onView(
            allOf(
                isA(Chip::class.java),
                anyOf(
                    withParent(withId(R.id.chipGroupSecondLevelOne)),
                    withParent(withId(R.id.chipGroupSecondLevelTwo))
                ),
                textContainsString(sensor),
                isDisplayed()
            )
        )
            .perform(click())
    }

    private fun goBack() {
        onView(
            allOf(
                isA(AppCompatImageButton::class.java),
                withParent(withId(R.id.topAppBar))
            )
        ).perform(click())
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

    private fun launchMapScreen() {
        val args = bundleOf(
            "address" to "Surgut",
            "lat" to "61.264426",
            "lng" to "73.406232",
            "txtParameter" to measurementTypePM,
            "txtSensor" to airbeam
        )
        mapScenario = launchFragmentInContainer(args, R.style.Theme_Aircasting, factory = fragmentFactory)
    }
}