package pl.llp.aircasting.data.model.observers

import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import pl.llp.aircasting.data.local.entity.SessionWithStreamsAndMeasurementsDBObject
import pl.llp.aircasting.data.local.entity.StreamWithMeasurementsDBObject
import pl.llp.aircasting.data.model.Measurement
import pl.llp.aircasting.utilities.DatabaseObjects.measurementDBObject
import pl.llp.aircasting.utilities.DatabaseObjects.measurementStreamDBObject
import pl.llp.aircasting.utilities.DatabaseObjects.sessionDBObject
import kotlin.test.assertEquals

class FixedSessionsObserverTest {

    @Test
    fun buildSession_returnsSessionWithCorrectMeasurements() {
        val fixedSessionsObserver = mock<FixedSessionsObserver>()
        whenever(fixedSessionsObserver.buildSession(any())).thenCallRealMethod()
        val measurementDBObject = measurementDBObject
        val streamWithMeasurementsDBObject = mock<StreamWithMeasurementsDBObject> {
            on { stream } doReturn measurementStreamDBObject
            on { measurements } doReturn listOf(measurementDBObject)
        }
        val sessionWithMeasurementsDBObject = mock<SessionWithStreamsAndMeasurementsDBObject> {
            on { session } doReturn sessionDBObject
            on { streams } doReturn listOf(streamWithMeasurementsDBObject)
        }

        val expectedMeasurements = listOf(Measurement(measurementDBObject))

        val session = fixedSessionsObserver.buildSession(sessionWithMeasurementsDBObject)
        val resultMeasurements = session.streams.first().measurements

        assertEquals(expectedMeasurements, resultMeasurements)
    }
}