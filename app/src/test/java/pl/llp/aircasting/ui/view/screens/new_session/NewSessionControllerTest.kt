package pl.llp.aircasting.ui.view.screens.new_session

import androidx.appcompat.app.AppCompatActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.internal.util.reflection.ReflectionMemberAccessor
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.util.Settings

@RunWith(MockitoJUnitRunner::class)
class NewSessionControllerTest {
    private val memberAccessor = ReflectionMemberAccessor()
    @Mock
    var controller: NewSessionController

    @Mock
    lateinit var session: Session

    @Mock
    lateinit var activity: AppCompatActivity

    @Mock
    lateinit var settings: Settings

    @Before
    fun setup() {
        val activityField = controller.javaClass.getDeclaredField("mContextActivity")
        memberAccessor.set(activityField, controller, activity)
        val settingsField = controller.javaClass.getDeclaredField("settings")
        memberAccessor.set(settingsField, controller, settings)
        whenever(controller.onStartRecordingClicked(anyOrNull())).thenCallRealMethod()
    }

    @Test
    fun onStartRecordingClicked_whenSessionTypeIsNotMobile_doesNotIncrementActiveMobileSessionCount() {
        whenever(session.type).thenReturn(Session.Type.FIXED)

        controller.onStartRecordingClicked(session)

        verify(settings, never()).increaseActiveMobileSessionsCount()
    }

    @Test
    fun onStartRecordingClicked_whenSessionTypeIsMobile_incrementsActiveMobileSessionCount() {
        whenever(session.type).thenReturn(Session.Type.MOBILE)

        controller.onStartRecordingClicked(session)

        verify(settings).increaseActiveMobileSessionsCount()
    }
}