package pl.llp.aircasting.ui.view.screens.dashboard

import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.fixed.ModifiableFixedSessionActionsBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.fixed.UnmodifiableFixedSessionActionsBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.mobile.active.MobileActiveSessionActionsBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.mobile.dormant.MobileDormantSessionActionsBottomSheet
import kotlin.test.assertIs

class SessionPresenterTest {

    @Test
    fun buildActionsBottomSheet_returnsCorrectBottomSheetFor_mobileActiveSession() {
        val session = mock<Session> {
            on { tab } doReturn SessionsTab.MOBILE_ACTIVE
        }
        val listener = mock<MobileActiveSessionActionsBottomSheet.Listener>()
        val presenter = SessionPresenter()
        presenter.session = session

        val bottomSheet = presenter.buildActionsBottomSheet(listener)

        assertIs<MobileActiveSessionActionsBottomSheet>(bottomSheet)
    }

    @Test
    fun buildActionsBottomSheet_returnsCorrectBottomSheetFor_mobileDormantSession() {
        val session = mock<Session> {
            on { tab } doReturn SessionsTab.MOBILE_DORMANT
        }
        val listener = mock<MobileActiveSessionActionsBottomSheet.Listener>()
        val presenter = SessionPresenter()
        presenter.session = session

        val bottomSheet = presenter.buildActionsBottomSheet(listener)

        assertIs<MobileDormantSessionActionsBottomSheet>(bottomSheet)
    }

    @Test
    fun buildActionsBottomSheet_returnsCorrectBottomSheetFor_fixedInternalSession() {
        val session = mock<Session> {
            on { tab } doReturn SessionsTab.FIXED
            on { isExternal } doReturn false
        }
        val sessionFollowed = mock<Session> {
            on { tab } doReturn SessionsTab.FOLLOWING
            on { isExternal } doReturn false
        }
        val listener = mock<MobileActiveSessionActionsBottomSheet.Listener>()
        val presenter = SessionPresenter()

        presenter.session = session
        val bottomSheet = presenter.buildActionsBottomSheet(listener)
        presenter.session = sessionFollowed
        val bottomSheetFollowing = presenter.buildActionsBottomSheet(listener)

        assertIs<ModifiableFixedSessionActionsBottomSheet>(bottomSheet)
        assertIs<ModifiableFixedSessionActionsBottomSheet>(bottomSheetFollowing)
    }

    @Test
    fun buildActionsBottomSheet_returnsCorrectBottomSheetFor_fixedExternalSession() {
        val session = mock<Session> {
            on { tab } doReturn SessionsTab.FIXED
            on { isExternal } doReturn true
        }
        val sessionFollowed = mock<Session> {
            on { tab } doReturn SessionsTab.FOLLOWING
            on { isExternal } doReturn true
        }
        val listener = mock<MobileActiveSessionActionsBottomSheet.Listener>()
        val presenter = SessionPresenter()

        presenter.session = session
        val bottomSheet = presenter.buildActionsBottomSheet(listener)
        presenter.session = sessionFollowed
        val bottomSheetFollowing = presenter.buildActionsBottomSheet(listener)

        assertIs<UnmodifiableFixedSessionActionsBottomSheet>(bottomSheet)
        assertIs<UnmodifiableFixedSessionActionsBottomSheet>(bottomSheetFollowing)
    }
}