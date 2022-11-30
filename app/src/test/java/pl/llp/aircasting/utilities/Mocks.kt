package pl.llp.aircasting.utilities

import org.mockito.kotlin.KStubbing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter

inline fun mockPresenter(stubbing: KStubbing<Session>.(Session) -> Unit): SessionPresenter {
    val session = mock<Session>()
    session.apply { KStubbing(session).stubbing(session) }
    return mock {
        on { this.session } doReturn session
    }
}