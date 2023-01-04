package pl.llp.aircasting.ui.view.screens.dashboard

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.llp.aircasting.R
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.fixed.ModifiableFixedSessionActionsBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.mobile.dormant.MobileDormantSessionActionsBottomSheet

@RunWith(AndroidJUnit4::class)
class SessionActionsBottomSheetTest {

    private lateinit var fixedBottomSheetScenario: FragmentScenario<ModifiableFixedSessionActionsBottomSheet>
    private lateinit var bottomSheetScenario: FragmentScenario<MobileDormantSessionActionsBottomSheet>

    @Before
    fun setup() {
        Thread.sleep(1000)
    }

    @Test
    fun testFixedSessionActionsBottomSheet_contains_createThresholdAlertButton() {
        fixedBottomSheetScenario = launchFragment(themeResId = R.style.Theme_Aircasting_Test)

        onView(withId(R.id.create_threshold_alert_button))
            .check(matches(isDisplayed()))

        onView(withId(R.id.cancel_button))
            .perform(click())
        fixedBottomSheetScenario.close()
    }

    @Test
    fun testSessionActionsBottomSheet_doesNotContain_createThresholdAlertButton() {
        bottomSheetScenario = launchFragment(themeResId = R.style.Theme_Aircasting_Test)

        onView(withId(R.id.create_threshold_alert_button))
            .check(doesNotExist())

        onView(withId(R.id.cancel_button))
            .perform(click())
        bottomSheetScenario.close()
    }
}