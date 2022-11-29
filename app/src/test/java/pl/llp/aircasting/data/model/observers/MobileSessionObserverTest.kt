package pl.llp.aircasting.data.model.observers

import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndNotesDBObject
import pl.llp.aircasting.data.local.entity.StreamWithMeasurementsDBObject
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.utilities.DatabaseObjects
import pl.llp.aircasting.utilities.DatabaseObjects.measurementDBObject
import kotlin.test.assertEquals


class MobileSessionObserverTest {
    @Test
    fun buildSession_returnsSessionWithCorrectMeasurements() {
        val fixedSessionsObserver = mock<MobileSessionObserver>()
        whenever(fixedSessionsObserver.buildSession(any())).thenCallRealMethod()
        val streamWithMeasurementsDBObject = mock<StreamWithMeasurementsDBObject> {
            on { stream } doReturn DatabaseObjects.measurementStreamDBObject
            on { measurements } doReturn listOf(measurementDBObject)
        }
        val sessionWithNotes = mock<SessionWithStreamsAndNotesDBObject> {
            on { session } doReturn DatabaseObjects.sessionDBObject
            on { streams } doReturn listOf(streamWithMeasurementsDBObject)
            on { notes } doReturn mutableListOf(DatabaseObjects.noteDBObject)
        }
        val expectedMeasurements = listOf(Measurement(measurementDBObject))

        val session = fixedSessionsObserver.buildSession(sessionWithNotes)
        val resultMeasurements = session.streams.first().measurements

        assertEquals(expectedMeasurements, resultMeasurements)
    }
}