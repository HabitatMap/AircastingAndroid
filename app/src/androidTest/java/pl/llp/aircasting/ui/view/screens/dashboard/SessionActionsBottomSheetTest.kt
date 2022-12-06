package pl.llp.aircasting.ui.view.screens.dashboard

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import pl.llp.aircasting.R

@RunWith(AndroidJUnit4::class)
class SessionActionsBottomSheetTest {

    private lateinit var fixedBottomSheetScenario: FragmentScenario<FixedSessionActionsBottomSheet>
    private lateinit var bottomSheetScenario: FragmentScenario<SessionActionsBottomSheet>

    @Test
    fun fixedSessionActionsBottomSheet_contains_createThresholdAlertButton() {
        fixedBottomSheetScenario = launchFragment(themeResId = R.style.Theme_Aircasting)

        onView(withId(R.id.create_threshold_alert_button))
            .check(matches(isDisplayed()))

        fixedBottomSheetScenario.close()
    }
    @Test
    fun sessionActionsBottomSheet_doesNotContain_createThresholdAlertButton() {
        bottomSheetScenario = launchFragment(themeResId = R.style.Theme_Aircasting)

        onView(withId(R.id.create_threshold_alert_button))
            .check(doesNotExist())

        bottomSheetScenario.close()
    }
}