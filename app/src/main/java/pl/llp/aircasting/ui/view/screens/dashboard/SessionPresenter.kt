package pl.llp.aircasting.ui.view.screens.dashboard

import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.SensorName
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.dashboard.charts.ChartData
import java.util.*

class SessionPresenter() {
    var session: Session? = null
    var selectedStream: MeasurementStream? = null
    var sensorThresholds: Map<String, SensorThreshold> = hashMapOf()
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
        sensorThresholds: Map<String, SensorThreshold>,
        selectedStream: MeasurementStream? = null,
        expanded: Boolean = false,
        loading: Boolean = false
    ) : this() {
        this.session = session
        this.selectedStream = selectedStream ?: defaultStream(session)
        this.expanded = expanded
        this.loading = loading
        this.sensorThresholds = sensorThresholds

        when (session.tab) {
            SessionsTab.FOLLOWING -> {
                this.chartData = ChartData(session)
                this.shouldHideMap = session.indoor
            }
            SessionsTab.FIXED -> this.shouldHideMap = session.indoor
            SessionsTab.MOBILE_ACTIVE -> {
                this.chartData = ChartData(session)
                this.shouldHideMap = session.locationless
                this.loading = true
                this.expanded = true
            }
            SessionsTab.MOBILE_DORMANT -> this.shouldHideMap = session.locationless
        }
    }

    constructor(sessionUUID: String, initialSensorName: String?) : this() {
        this.sessionUUID = sessionUUID
        this.initialSensorName = initialSensorName
    }

    fun selectedSensorThreshold(): SensorThreshold? {
        selectedStream ?: return null

        return sensorThresholds[selectedStream?.sensorName]
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

    fun isExternal(): Boolean = session?.isExternal == true

    fun isDisconnected(): Boolean {
        return session?.isDisconnected() == true
    }

    fun isDisconnectable(): Boolean {
        return session?.isAirBeam3() == true
    }

    fun allStreamsHaveLoaded(): Boolean {
        if (session?.isAirBeam() == true && session?.isExternal == false)
            return session?.streams?.size == 5

        return true
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
            val sortedByDetailedType = session?.streamsSortedByDetailedType()
            val pm2point5 =
                sortedByDetailedType?.find { it.detailedType == SensorName.PM2_5.detailedType }
            val firstStream = sortedByDetailedType?.firstOrNull()

            return pm2point5 ?: firstStream
        }
    }
}
