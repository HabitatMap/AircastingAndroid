package pl.llp.aircasting

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.core.AllOf.allOf
import org.junit.*
import org.junit.runner.RunWith
import pl.llp.aircasting.data.api.services.ApiServiceFactory
import pl.llp.aircasting.data.api.util.StringConstants.airbeam
import pl.llp.aircasting.data.api.util.StringConstants.measurementTypeOzone
import pl.llp.aircasting.data.api.util.StringConstants.measurementTypePM
import pl.llp.aircasting.data.api.util.StringConstants.openAQ
import pl.llp.aircasting.data.api.util.StringConstants.purpleAir
import pl.llp.aircasting.data.local.AppDatabase

import pl.llp.aircasting.di.TestApiModule
import pl.llp.aircasting.di.TestPermissionsModule
import pl.llp.aircasting.di.TestSettingsModule
import pl.llp.aircasting.di.modules.AppModule
import pl.llp.aircasting.di.modules.PermissionsModule
import pl.llp.aircasting.helpers.*
import pl.llp.aircasting.helpers.assertions.RecyclerViewItemCountAssertion
import pl.llp.aircasting.ui.view.adapters.FixedFollowAdapter
import pl.llp.aircasting.ui.view.fragments.search_follow_fixed_session.SearchLocationFragment
import pl.llp.aircasting.ui.view.fragments.search_follow_fixed_session.SearchLocationResultFragment
import pl.llp.aircasting.ui.view.screens.main.MainActivity
import pl.llp.aircasting.ui.view.screens.search.SearchFixedSessionActivity
import pl.llp.aircasting.util.Settings
import java.net.HttpURLConnection
import javax.inject.Inject

/*
* When running test suite, do not forget to disable animations on the test device
* Otherwise some test may fail
* */

@RunWith(AndroidJUnit4::class)
class SearchFollowTest {
    companion object {
        private lateinit var startIntent: Intent
        private lateinit var searchIntent: Intent

        @BeforeClass
        @JvmStatic
        fun setupIntents() {
            startIntent =
                Intent(
                    ApplicationProvider.getApplicationContext<AircastingApplication>(),
                    MainActivity::class.java
                )
            searchIntent =
                Intent(
                    ApplicationProvider.getApplicationContext<AircastingApplication>(),
                    SearchFixedSessionActivity::class.java
                )
        }
    }

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var server: MockWebServer

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var fragmentFactory: FragmentFactory

    lateinit var mainActivityScenario: ActivityScenario<MainActivity>
    lateinit var searchActivityScenario: ActivityScenario<SearchFixedSessionActivity>

    lateinit var searchScenario: FragmentScenario<SearchLocationFragment>
    lateinit var mapScenario: FragmentScenario<SearchLocationResultFragment>

    private val newYork = "New York"
    private val newYorkArgs = bundleOf(
        "address" to newYork,
        "lat" to "40.692985",
        "lng" to "-73.964609",
        "txtParameter" to measurementTypePM,
        "txtSensor" to openAQ
    )

    @Before
    fun setup() {
        setupDagger()

        server.start()

        val newYorkResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(Util.readFile("NewYork.json"))

        val newYorkDownloadSessionResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(Util.readFile("HabitatMap-WiFi.json"))

        MockWebServerDispatcher.setNotFullPath(
            mapOf(
                "/api/fixed/active/sessions.json" to newYorkResponse,
                "/api/fixed/sessions/" to newYorkDownloadSessionResponse
            ),
            server
        )

        settings.login("NAME", "EMAIL", "TOKEN")
    }

    private fun setupDagger() {
        val app = ApplicationProvider.getApplicationContext<AircastingApplication>()
        val permissionsModule = TestPermissionsModule()
        val testAppComponent = DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .apiModule(TestApiModule())
            .settingsModule(TestSettingsModule())
            .permissionsModule(permissionsModule)
            .build()
        app.userDependentComponent = testAppComponent
        testAppComponent.inject(this)
    }

    @After
    fun cleanup() {
        server.shutdown()
        database.close()
    }

    @Test
    fun whenClickingSearchButton_goesToSearchFixedSessionsScreen() {
        mainActivityScenario = ActivityScenario.launch(startIntent)

        onView(withId(R.id.search_follow_icon))
            .perform(click())

        onView(withId(R.id.places_autocomplete_search_input))
            .check(matches(isDisplayed()))
        onView(withId(R.id.chipGroupFirstLevel))
            .check(matches(isDisplayed()))

        mainActivityScenario.close()
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
        mainActivityScenario = ActivityScenario.launch(startIntent)
        onView(withId(R.id.search_follow_icon))
            .perform(click())

        searchAndValidateDisplayedParameters(newYork, measurementTypePM, airbeam)
        searchAndValidateDisplayedParameters(newYork, measurementTypePM, openAQ)
        searchAndValidateDisplayedParameters(newYork, measurementTypePM, purpleAir)
        searchAndValidateDisplayedParameters(newYork, measurementTypeOzone, openAQ)

        mainActivityScenario.close()
    }

    @Test
    fun sessionsFoundNumber_shouldBeEqualToCardsNumber() {
        launchMapScreen(newYorkArgs)

        var cardsCount = 0
        awaitForAssertion {
            onView(withId(R.id.recyclerFixedFollow))
                .check(RecyclerViewItemCountAssertion {
                    cardsCount = it
                    it > 0
                })
        }

        onView(withId(R.id.txtShowingSessionsNumber))
            .check(matches(textContainsString("of $cardsCount")))

        mapScenario.close()
    }


    @Test
    fun whenThereNoSessionsFound_numberShouldBeZero_listShouldBeEmpty() {
        val noSessionsAreaArgs = bundleOf(
            "address" to "Surgut",
            "lat" to "61.265459",
            "lng" to "73.416532",
            "txtParameter" to measurementTypePM,
            "txtSensor" to airbeam
        )
        val noSessionsAreaResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(Util.readFile("NoSessionsArea.json"))

        MockWebServerDispatcher.setNotFullPath(
            mapOf(
                "/api/fixed/active/sessions.json" to noSessionsAreaResponse,
            ),
            server
        )
        launchMapScreen(noSessionsAreaArgs)
        waitForSessionData()

        onView(withId(R.id.recyclerFixedFollow))
            .check(RecyclerViewItemCountAssertion {
                it == 0
            })
        onView(withId(R.id.txtShowingSessionsNumber))
            .check(matches(textContainsString("of 0")))

        mapScenario.close()
    }

    @Test
    fun whenGoingBackFromMapScreen_searchScreenRetainsAddress() {
        searchActivityScenario = ActivityScenario.launch(searchIntent)
        searchForPlace(newYork)
        selectSensor(measurementTypePM, airbeam)
        goToMapScreen()
        Espresso.pressBack()

        searchFieldHasHint(newYork)

        searchActivityScenario.close()
    }

    @Test
    fun whenPressingFinishButtonOnMapScreen_goesToDashboard() {
        launchMapScreen(newYorkArgs)
        onView(withId(R.id.finishSearchButton))
            .perform(click())

        onView(withId(R.id.dashboard))
            .check(matches(isDisplayed()))

        mapScenario.close()
    }

    @Test
    fun whenChoosingCard_bottomSheetHasSameDateAndTitleAsCard_chipsSwitchGraphView_externalSessionIsFollowed_Unfollowed() {
        searchActivityScenario = ActivityScenario.launch(searchIntent)

        searchForPlace(newYork)
        selectSensor(measurementTypePM, openAQ)
        goToMapScreen()
        waitForSessionData()

        var cardTitle = ""
        var cardDate = ""
        onView(withId(R.id.recyclerFixedFollow))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<FixedFollowAdapter.DataViewHolder>(0,
                    getSessionTitleAndDateAction { title, date ->
                        cardTitle = title; cardDate = date
                    }
                )
            )
            .perform(click())

        verifySessionTitleAndDate(cardTitle, cardDate)
        verifySwitchingChipsWork()
        verifyFollowingOfExternalSession(cardTitle)

        searchActivityScenario.close()
    }

    private fun verifyFollowingOfExternalSession(cardTitle: String) {
        onView(withId(R.id.followBtn))
            .perform(click())

        onView(isRoot()).perform(waitFor(500))

        Espresso.pressBack()

        onView(withId(R.id.finishSearchButton))
            .perform(click())

        waitAndRetry {
            onView(allOf(withId(R.id.session_name), isDisplayed()))
                .check(matches(withText(cardTitle)))
        }
    }

    @Test
    fun followingYourOwnSession_followButtonIsDisabled() {
        settings.login("HabitatMap", "EMAIL", "TOKEN")
        searchActivityScenario = ActivityScenario.launch(searchIntent)

        searchForPlace(newYork)
        selectSensor(measurementTypePM, openAQ)
        goToMapScreen()
        waitForSessionData()

        onView(withId(R.id.recyclerFixedFollow))
            .perform(clickOnFirstItem())

        onView(withId(R.id.followBtn))
            .check(matches(not(isEnabled())))

        searchActivityScenario.close()
    }

    private fun verifySessionTitleAndDate(cardTitle: String, cardDate: String) {
        onView(withId(R.id.txtTitle))
            .check(matches(textContainsString(cardTitle)))
        onView(withId(R.id.txtDate))
            .check(matches(textContainsString(cardDate)))
    }

    private fun verifySwitchingChipsWork() {
        onView(withId(R.id.chartChip))
            .perform(click())
        onView(withId(R.id.chart_view))
            .check(matches(isDisplayed()))
        onView(withId(R.id.mapChip))
            .perform(click())
        onView(withId(R.id.chart_view))
            .check(matches(not(isDisplayed())))
    }

    private fun getSessionTitleAndDateAction(saveTo: (String, String) -> Unit): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = CoreMatchers.allOf(
                isDisplayed(),
                isAssignableFrom(MaterialCardView::class.java)
            )

            override fun getDescription(): String = "Gets date and title on the card"

            override fun perform(uiController: UiController, view: View) {
                val title = view.findViewById<TextView>(R.id.textView).text.toString()
                val date = view.findViewById<TextView>(R.id.textView2).text.toString()
                saveTo(title, date)
            }
        }
    }

    private fun waitForSessionData() {
        awaitForAssertion {
            onView(withId(R.id.txtShowingSessionsNumber)).check(matches(isDisplayed()))
        }
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

    private fun searchFieldHasText(hint: String) {
        onView(withId(R.id.places_autocomplete_search_input))
            .check(matches(textContainsString(hint)))
    }

    private fun launchSearchScreen() {
        searchScenario = launchFragmentInContainer(
            themeResId = R.style.Theme_Aircasting,
            factory = fragmentFactory
        )
    }

    private fun launchMapScreen(args: Bundle) {
        mapScenario =
            launchFragmentInContainer(args, R.style.Theme_Aircasting, factory = fragmentFactory)
    }
}