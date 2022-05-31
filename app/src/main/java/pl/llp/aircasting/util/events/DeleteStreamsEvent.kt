package pl.llp.aircasting.util.events

import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.Session

class DeleteStreamsEvent(val session: Session, val streamsToDelete: List<MeasurementStream>?)

