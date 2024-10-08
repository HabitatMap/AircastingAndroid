package pl.llp.aircasting.ui.view.screens.dashboard.reordering_following

import androidx.recyclerview.widget.SortedList
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.spy
import org.mockito.internal.util.reflection.ReflectionMemberAccessor
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.data.model.observers.SessionsObserver
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.ui.view.screens.dashboard.SessionsTab
import pl.llp.aircasting.ui.view.screens.dashboard.following.FollowingRecyclerAdapter
import pl.llp.aircasting.utilities.mockPresenter
import java.lang.reflect.Field
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(MockitoJUnitRunner::class)
class ReorderingFollowingRecyclerAdapterTest {

    private lateinit var adapter: ReorderingFollowingRecyclerAdapter

    private lateinit var sessionPresenters: SortedList<SessionPresenter>

    @Mock
    private lateinit var callback: FollowingRecyclerAdapter.FollowingModificationCallback

    @Mock
    private lateinit var sessionDismissCallback: (session: Session) -> Unit

    @Mock
    private lateinit var sessionUpdateFollowedAtCallback: (session: Session) -> Unit

    private val firstDate = Date(2L)
    private val secondDate = Date(1L)
    private val firstPresenter = mockPresenter {
        on { followedAt } doReturn firstDate
    }
    private val secondPresenter = mockPresenter {
        on { followedAt } doReturn secondDate
    }
    private val thirdPresenter = mockPresenter {}

    @Before
    fun setup() {
        sessionPresenters = spy(SortedList(SessionPresenter::class.java, callback))

        adapter = ReorderingFollowingRecyclerAdapter(
            null,
            mock(),
            mock(),
            mock(),
            mock(),
            sessionDismissCallback,
            sessionUpdateFollowedAtCallback,
        )
        val adapterAllFields = getAllFields(mutableListOf(), adapter.javaClass)
        val sessionPresentersField = adapterAllFields?.find { it?.name == "mSessionPresenters" }

        val memberAccessor = ReflectionMemberAccessor()
        memberAccessor.set(sessionPresentersField, adapter, sessionPresenters)

        sessionPresenters.addAll(firstPresenter, secondPresenter, thirdPresenter)
    }

    @Test
    fun presenters_shouldHaveCollapsedState() {
        val session = mock<Session> {
            on { tab } doReturn SessionsTab.FIXED
            on { indoor } doReturn true
        }

        adapter.bindSessions(
            mapOf(SessionsObserver.ModificationType.INSERTED to listOf(session)),
            mock()
        )

        assertFalse(sessionPresenters[0].expanded)
    }

    @Test
    fun onItemMove_shouldSwapPresentersInAdapterDataset() {
        adapter.onItemMove(0, 1)

        assertEquals(secondPresenter, sessionPresenters[0])
        assertEquals(firstPresenter, sessionPresenters[1])
    }

    @Test
    fun onItemMove_shouldRecalculatePositions_invokesUpdateFollowedAtCallback() {
        adapter.onItemMove(0, 1)

        inOrder(sessionPresenters, sessionUpdateFollowedAtCallback) {
            verify(sessionPresenters).recalculatePositionOfItemAt(0)
            verify(sessionPresenters).recalculatePositionOfItemAt(1)
            verify(sessionUpdateFollowedAtCallback).invoke(firstPresenter.session!!)
            verify(sessionUpdateFollowedAtCallback).invoke(secondPresenter.session!!)
        }
    }

    @Test
    fun onItemDismiss_deletesItemFromList() {
        adapter.onItemDismiss(1)

        assertEquals(2, sessionPresenters.size())
        assertEquals(SortedList.INVALID_POSITION, sessionPresenters.indexOf(secondPresenter))
        assertEquals(thirdPresenter, sessionPresenters[1])
    }

    @Test
    fun onItemDismiss_invokesSessionDismissCallback() {
        adapter.onItemDismiss(1)

        verify(sessionDismissCallback).invoke(secondPresenter.session!!)
    }

    private fun getAllFields(fields: MutableList<Field?>, type: Class<*>): List<Field?>? {
        fields.addAll(type.declaredFields)
        if (type.superclass != null) {
            getAllFields(fields, type.superclass)
        }
        return fields
    }
}