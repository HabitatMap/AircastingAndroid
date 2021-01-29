package io.lunarlogic.aircasting.events

import io.lunarlogic.aircasting.models.MeasurementStream
import io.lunarlogic.aircasting.models.Session

class DeleteStreamsEvent(val session: Session, val streamsToDelete: List<MeasurementStream>?)

