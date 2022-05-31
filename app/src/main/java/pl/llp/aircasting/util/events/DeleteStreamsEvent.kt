package pl.llp.aircasting.util.events

import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.LocalSession

class DeleteStreamsEvent(val localSession: LocalSession, val streamsToDelete: List<MeasurementStream>?)

