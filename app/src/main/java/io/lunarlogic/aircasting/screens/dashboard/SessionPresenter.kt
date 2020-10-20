package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.screens.dashboard.charts.ChartData
import io.lunarlogic.aircasting.sensor.MeasurementStream
import io.lunarlogic.aircasting.sensor.SensorThreshold
import io.lunarlogic.aircasting.sensor.Session

class SessionPresenter() {
    var session: Session? = null
    var selectedStream: MeasurementStream? = null
    var sensorThresholds: HashMap<String, SensorThreshold> = hashMapOf()
    var expanded: Boolean = false
    var loading: Boolean = false
    var chartData: ChartData? = null

    constructor(
        session: Session,
        sensorThresholds: HashMap<String, SensorThreshold>,
        selectedStream: MeasurementStream? = null,
        expanded: Boolean = false,
        loading: Boolean = false
    ): this() {
        this.session = session
        this.selectedStream = selectedStream ?: defaultStream(session)
        this.expanded = expanded
        this.loading = loading
        this.sensorThresholds = sensorThresholds
        this.chartData = ChartData(session)
    }

    fun selectedSensorThreshold(): SensorThreshold? {
        selectedStream ?: return null

        return sensorThresholds[selectedStream!!.sensorName]
    }

    fun sensorThresholdFor(stream: MeasurementStream?): SensorThreshold? {
        stream ?: return null
        return sensorThresholds[stream.sensorName]
    }

    fun setDefaultStream() {
        selectedStream = defaultStream(session)
    }

    fun isFixed(): Boolean {
        return session?.isFixed() == true
    }

    fun isRecording(): Boolean {
        return session?.isRecording() == true
    }

    fun setSensorThresholds(sensorThresholds: List<SensorThreshold>) {
        val hash = hashMapOf<String, SensorThreshold>()
        sensorThresholds.forEach {
            hash[it.sensorName] = it
        }

        this.sensorThresholds = hash
    }

    companion object {
        fun defaultStream(session: Session?): MeasurementStream? {
            return session?.streamsSortedByDetailedType()?.firstOrNull()
        }
    }
}
