package pl.llp.aircasting.ui.view.screens.dashboard

import pl.llp.aircasting.data.model.MeasurementStream
import pl.llp.aircasting.data.model.SensorName
import pl.llp.aircasting.data.model.SensorThreshold
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.common.BottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.fixed.ModifiableFixedSessionActionsBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.fixed.UnmodifiableFixedSessionActionsBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.mobile.active.MobileActiveSessionActionsBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.mobile.dormant.MobileDormantSessionActionsBottomSheet
import pl.llp.aircasting.ui.view.screens.dashboard.charts.ChartData
import pl.llp.aircasting.util.SelectedStreams
import java.util.Date

class SessionPresenter() {
    var session: Session? = null
    private var mSelectedStream: MeasurementStream? = selectStream(session)
    val selectedStream get() = mSelectedStream
    var sensorThresholds: Map<String, SensorThreshold> = hashMapOf()
    var expanded: Boolean = false
    var loading: Boolean = false
    var chartData: ChartData? = null
    var sessionUUID: String? = session?.uuid
    var initialSensorName: String? = null
    var visibleTimeSpan: ClosedRange<Date>? = null
    var shouldHideMap: Boolean = false

    constructor(
        session: Session,
        sensorThresholds: Map<String, SensorThreshold>,
        expanded: Boolean = false,
        loading: Boolean = false
    ) : this() {
        this.session = session
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

    constructor(session: Session) : this() {
        this.session = session
        this.sessionUUID = session.uuid
    }

    fun select(stream: MeasurementStream?) {
        mSelectedStream = stream
        SelectedStreams.save(this)
    }

    fun selectedSensorThreshold(): SensorThreshold? {
        selectedStream ?: return null

        return sensorThresholds[selectedStream?.sensorName]
    }

    fun sensorThresholdFor(stream: MeasurementStream?): SensorThreshold? {
        stream ?: return null
        return sensorThresholds[stream.sensorName]
    }

    fun updateSelectedStream() {
        mSelectedStream = selectStream(session)
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

    fun isNotMobileActive() = !isMobileActive()

    fun isRecording(): Boolean {
        return session?.isRecording() == true
    }

    fun isExternal(): Boolean = session?.isExternal == true

    fun isDisconnected(): Boolean {
        return session?.isDisconnected() == true
    }

    fun isDisconnectable(): Boolean {
        return session?.isDisconnectable() == true
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

    fun buildActionsBottomSheet(): BottomSheet? {
        return when (session?.tab) {
            SessionsTab.MOBILE_ACTIVE ->
                MobileActiveSessionActionsBottomSheet(
                    this
                )
            SessionsTab.MOBILE_DORMANT ->
                MobileDormantSessionActionsBottomSheet(
                    session
                )
            SessionsTab.FIXED, SessionsTab.FOLLOWING ->
                if (isExternal())
                    UnmodifiableFixedSessionActionsBottomSheet(
                        session
                    )
                else
                    ModifiableFixedSessionActionsBottomSheet(
                        session
                    )
            else -> null
        }
    }

    companion object {
        private fun selectStream(session: Session?): MeasurementStream? {
            val sortedByDetailedType = session?.streamsSortedByDetailedType()
            val savedStreamDetailedType = SelectedStreams.get(session?.uuid)

            val savedStream =
                sortedByDetailedType?.find { it.detailedType == savedStreamDetailedType }
            val pm2point5 =
                sortedByDetailedType?.find { it.detailedType == SensorName.PM2_5.detailedType }
            val firstStream = sortedByDetailedType?.firstOrNull()

            return savedStream ?: pm2point5 ?: firstStream
        }
    }
}