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
import pl.llp.aircasting.util.helpers.sensor.airbeamSyncable.configurator.AirBeamMiniV2StateRepository

@RunWith(MockitoJUnitRunner::class)
class NewSessionControllerTest {
    private val memberAccessor = ReflectionMemberAccessor()
    @Mock
    lateinit var controller: NewSessionController

    @Mock
    lateinit var session: Session

    @Mock
    lateinit var activity: AppCompatActivity

    @Mock
    lateinit var settings: Settings

    @Mock
    lateinit var v2StateRepository: AirBeamMiniV2StateRepository

    @Before
    fun setup() {
        val activityField = controller.javaClass.getDeclaredField("mContextActivity")
        memberAccessor.set(activityField, controller, activity)
        val settingsField = controller.javaClass.getDeclaredField("settings")
        memberAccessor.set(settingsField, controller, settings)
        val firmwareVersionField = controller.javaClass.getDeclaredField("deviceFirmwareVersion")
        memberAccessor.set(firmwareVersionField, controller, pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem.FirmwareVersion.V1)
        val v2StateRepositoryField = controller.javaClass.getDeclaredField("v2StateRepository")
        memberAccessor.set(v2StateRepositoryField, controller, v2StateRepository)
        whenever(controller.onStartRecordingClicked(anyOrNull())).thenCallRealMethod()
    }

    @Test
    fun onSetAppropriateStatusForStartOfRecordingClicked_whenSessionTypeIsNotMobile_doesNotIncrementActiveMobileSessionCount() {
        whenever(session.type).thenReturn(Session.Type.FIXED)

        controller.onStartRecordingClicked(session)

        verify(settings, never()).increaseActiveMobileSessionsCount()
    }

    @Test
    fun onSetAppropriateStatusForStartOfRecordingClicked_whenSessionTypeIsMobile_incrementsActiveMobileSessionCount() {
        whenever(session.type).thenReturn(Session.Type.MOBILE)

        controller.onStartRecordingClicked(session)

        verify(settings).increaseActiveMobileSessionsCount()
    }
}