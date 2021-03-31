package io.lunarlogic.aircasting.screens.dashboard

import io.lunarlogic.aircasting.models.*
import io.lunarlogic.aircasting.screens.dashboard.charts.ChartData
import java.util.*
import kotlin.collections.HashMap

class SessionPresenter() {
    var session: Session? = null
    var selectedStream: MeasurementStream? = null
    var sensorThresholds: HashMap<String, SensorThreshold> = hashMapOf()
    var expanded: Boolean = false
    var loading: Boolean = false
    var reconnecting: Boolean = false
    var chartData: ChartData? = null
    var sessionUUID: String? = null
    var initialSensorName: String? = null
    var visibleTimeSpan: ClosedRange<Date>? = null
    var shouldHideMap: Boolean = false

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

        if (session.tab == SessionsTab.FOLLOWING || session.tab == SessionsTab.MOBILE_ACTIVE) {
            this.chartData = ChartData(session)
        }
        this.shouldHideMap = session.indoor

        if (session.tab == SessionsTab.MOBILE_ACTIVE) {
            this.loading = true
        }
    }

    constructor(sessionUUID: String, initialSensorName: String?): this() {
        this.sessionUUID = sessionUUID
        this.initialSensorName = initialSensorName
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

    fun isMobileDormant(): Boolean {
        return !isFixed() && !isRecording()
    }

    fun isMobileActive(): Boolean {
        return !isFixed() && isRecording()
    }

    fun isRecording(): Boolean {
        return session?.isRecording() == true
    }

    fun isDisconnected(): Boolean {
        return session?.isDisconnected() == true
    }

    fun isDisconnectable(): Boolean {
        return session?.isAirBeam3() == true
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
