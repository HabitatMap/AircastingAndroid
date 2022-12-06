package pl.llp.aircasting.ui.view.screens.dashboard.fixed

import androidx.fragment.app.FragmentManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.internal.util.reflection.ReflectionMemberAccessor
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.llp.aircasting.ui.view.screens.dashboard.theshold_alerts.CreateThresholdAlertBottomSheet

@RunWith(MockitoJUnitRunner::class)
class FixedSessionViewMvcImplTest {
    @Mock
    lateinit var fragmentManager: FragmentManager

    @Mock
    lateinit var createThresholdAlertBottomSheet: CreateThresholdAlertBottomSheet

    @Mock
    lateinit var viewMvcImpl: FixedSessionViewMvcImpl

    @Before
    fun setup() {
        val reflectionMemberAccessor = ReflectionMemberAccessor()
        val bottomSheetField =
            viewMvcImpl.javaClass.getDeclaredField("createThresholdAlertBottomSheet")
        reflectionMemberAccessor.set(bottomSheetField, viewMvcImpl, createThresholdAlertBottomSheet)

        val fragmentManagerField =
            viewMvcImpl.javaClass.getDeclaredField("supportFragmentManager")
        reflectionMemberAccessor.set(fragmentManagerField, viewMvcImpl, fragmentManager)
    }

    @Test
    fun createThresholdAlertPressed_shouldShowThresholdAlertBottomSheet() {
        whenever(viewMvcImpl.createThresholdAlertPressed()).thenCallRealMethod()

        viewMvcImpl.createThresholdAlertPressed()

        verify(createThresholdAlertBottomSheet).show(fragmentManager)
    }
}