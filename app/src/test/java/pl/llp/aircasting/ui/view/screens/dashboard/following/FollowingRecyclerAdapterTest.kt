package pl.llp.aircasting.ui.view.screens.dashboard.following

import androidx.recyclerview.widget.SortedList
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import pl.llp.aircasting.ui.view.screens.dashboard.SessionPresenter
import pl.llp.aircasting.utilities.mockPresenter
import java.util.*
import kotlin.test.assertEquals

class FollowingRecyclerAdapterTest {

    @Test
    fun sessionsPresenters_shouldBeOrderedBy_followedAt_inReverse() {
        val callback = mock<FollowingRecyclerAdapter.FollowingModificationCallback>()
        whenever(callback.compare(any(), any())).thenCallRealMethod()
        val firstPresenter = mockPresenter {
            on { followedAt } doReturn Date(2L)
        }
        val secondPresenter = mockPresenter {
            on { followedAt } doReturn Date(1L)
        }
        val thirdPresenter = mockPresenter {
            on { followedAt } doReturn Date(0L)
        }
        val sortedList = SortedList(SessionPresenter::class.java, callback)

        sortedList.addAll(firstPresenter, thirdPresenter, secondPresenter)

        assertEquals(firstPresenter, sortedList[0])
        assertEquals(secondPresenter, sortedList[1])
        assertEquals(thirdPresenter, sortedList[2])
    }

    @Test
    fun whenFollowedAtIsNull_shouldRetainOrder() {
        val callback = mock<FollowingRecyclerAdapter.FollowingModificationCallback>()
        whenever(callback.compare(any(), any())).thenCallRealMethod()
        val firstPresenter = mockPresenter {
            on { followedAt } doReturn null
        }
        val secondPresenter = mockPresenter {
            on { followedAt } doReturn null
        }
        val thirdPresenter = mockPresenter {
            on { followedAt } doReturn null
        }
        val sortedList = SortedList(SessionPresenter::class.java, callback)

        sortedList.addAll(firstPresenter, secondPresenter, thirdPresenter)

        assertEquals(firstPresenter, sortedList[0])
        assertEquals(secondPresenter, sortedList[1])
        assertEquals(thirdPresenter, sortedList[2])
    }
}