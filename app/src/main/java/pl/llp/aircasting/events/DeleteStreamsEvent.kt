package pl.llp.aircasting.events

import pl.llp.aircasting.models.MeasurementStream
import pl.llp.aircasting.models.Session

class DeleteStreamsEvent(val session: Session, val streamsToDelete: List<MeasurementStream>?)

