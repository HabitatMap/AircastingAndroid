package pl.llp.aircasting.data.local.repositories

import org.junit.Test
import pl.llp.aircasting.data.model.Session
import kotlin.test.assertTrue


class SearchedFixedSessionsRepositoryTest {
    @Test
    fun whenGivenCoordinatesPair_whereNoSessionsArePresent_shouldReturnEmptyList() {
        val repository = SearchedFixedSessionsRepository()
        val lat = 0.0
        val lng = 0.0

        val result: List<Session> = repository.getSessionsInside(lat, lng)

        assertTrue(result.isEmpty())
    }
}