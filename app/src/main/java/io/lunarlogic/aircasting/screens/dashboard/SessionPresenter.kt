package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.SensorThreshold
import io.lunarlogic.aircasting.sensor.Session

class SessionPresenter {
    var session: Session
    var selectedStream: MeasurementStream?
    var sensorThresholds: HashMap<String, SensorThreshold> = hashMapOf()
    var expanded: Boolean
    var loading: Boolean

    constructor(
        session: Session,
        sensorThresholds: HashMap<String, SensorThreshold>,
        selectedStream: MeasurementStream? = null,
        expanded: Boolean = false,
        loading: Boolean = false
    ) {
        this.session = session
        this.selectedStream = selectedStream ?: defaultStream(session)
        this.expanded = expanded
        this.loading = loading
        this.sensorThresholds = sensorThresholds
    }

    fun selectedSensorThreshold(): SensorThreshold? {
        selectedStream ?: return null

        return sensorThresholds[selectedStream!!.sensorName]
    }

    fun sensorThresholdFor(stream: MeasurementStream): SensorThreshold? {
        return sensorThresholds[stream.sensorName]
    }

    fun setDefaultStream() {
        selectedStream = defaultStream(session)
    }

    fun isFixed(): Boolean {
        return session.isFixed()
    }

    fun isRecording(): Boolean {
        return session.isRecording()
    }

    companion object {
        fun get(
            session: Session,
            sensorThresholds: List<SensorThreshold>,
            selectedStream: MeasurementStream? = null,
            expanded: Boolean = false,
            loading: Boolean = false
        ): SessionPresenter {
            val hash = hashMapOf<String, SensorThreshold>()
            sensorThresholds.forEach {
                hash[it.sensorName] = it
            }

            return SessionPresenter(session, hash, selectedStream, expanded, loading)
        }

        fun defaultStream(session: Session): MeasurementStream? {
            return session.streamsSortedByDetailedType().firstOrNull()
        }
    }
}
