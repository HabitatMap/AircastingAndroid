package pl.llp.aircasting.ui.view.screens.dashboard

import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.LocalSession
import pl.llp.aircasting.ui.view.screens.dashboard.charts.ChartData
import java.util.*

class SessionPresenter() {
    var localSession: LocalSession? = null
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
        localSession: LocalSession,
        sensorThresholds: HashMap<String, SensorThreshold>,
        selectedStream: MeasurementStream? = null,
        expanded: Boolean = false,
        loading: Boolean = false
    ): this() {
        this.localSession = localSession
        this.selectedStream = selectedStream ?: defaultStream(localSession)
        this.expanded = expanded
        this.loading = loading
        this.sensorThresholds = sensorThresholds
        if (localSession.tab == SessionsTab.FOLLOWING || localSession.tab == SessionsTab.MOBILE_ACTIVE) {
            this.chartData = ChartData(localSession)
        }
        if (localSession.tab == SessionsTab.MOBILE_ACTIVE || localSession.tab == SessionsTab.MOBILE_DORMANT) {
            this.shouldHideMap = localSession.locationless
        }
        if (localSession.tab == SessionsTab.FIXED || localSession.tab == SessionsTab.FOLLOWING) {
            this.shouldHideMap = localSession.indoor
        }

        if (localSession.tab == SessionsTab.MOBILE_ACTIVE) {
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
        selectedStream = defaultStream(localSession)
    }

    fun isFixed(): Boolean {
        return localSession?.isFixed() == true
    }

    fun isMobileDormant(): Boolean {
        return !isFixed() && !isRecording()
    }

    fun isMobileActive(): Boolean {
        return !isFixed() && isRecording()
    }

    fun isRecording(): Boolean {
        return localSession?.isRecording() == true
    }

    fun isDisconnected(): Boolean {
        return localSession?.isDisconnected() == true
    }

    fun isDisconnectable(): Boolean {
        return localSession?.isAirBeam3() == true
    }

    fun setSensorThresholds(sensorThresholds: List<SensorThreshold>) {
        val hash = hashMapOf<String, SensorThreshold>()
        sensorThresholds.forEach {
            hash[it.sensorName] = it
        }

        this.sensorThresholds = hash
    }

    companion object {
        fun defaultStream(localSession: LocalSession?): MeasurementStream? {
            return localSession?.streamsSortedByDetailedType()?.firstOrNull()
        }
    }
}
