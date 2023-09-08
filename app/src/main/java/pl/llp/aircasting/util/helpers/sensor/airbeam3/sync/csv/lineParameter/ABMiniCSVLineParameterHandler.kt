package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.lineParameter

import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.CSVMeasurementStream
import javax.inject.Inject

class ABMiniCSVLineParameterHandler @Inject constructor() : CSVLineParameterHandler() {
    override val timeParameter: ABLineParameter = ABMiniLineParameter.Time
    override val dateParameter: ABLineParameter = ABMiniLineParameter.Date
    override val uuidParameter: ABLineParameter = ABMiniLineParameter.UUID
    override val deviceType: DeviceItem.Type = DeviceItem.Type.AIRBEAMMINI

    override val supportedStreams: Map<ABLineParameter, CSVMeasurementStream> = hashMapOf(
        ABMiniLineParameter.PM1 to CSVMeasurementStream(
            "${CSVMeasurementStream.DEVICE_NAME}-PM1",
            PM_MEASUREMENT_TYPE,
            PM_MEASUREMENT_SHORT_TYPE,
            PM_UNIT_NAME,
            PM_UNIT_SYMBOL,
            0,
            12,
            35,
            55,
            150
        ),
        ABMiniLineParameter.PM2_5 to CSVMeasurementStream(
            "${CSVMeasurementStream.DEVICE_NAME}-PM2.5",
            PM_MEASUREMENT_TYPE,
            PM_MEASUREMENT_SHORT_TYPE,
            PM_UNIT_NAME,
            PM_UNIT_SYMBOL,
            0,
            12,
            35,
            55,
            150
        ),
    )

    sealed class ABMiniLineParameter(override val position: Int) : ABLineParameter(position) {
        object UUID : ABMiniLineParameter(0)
        object Date : ABMiniLineParameter(1)
        object Time : ABMiniLineParameter(2)
        object PM1 : ABMiniLineParameter(3)
        object PM2_5 : ABMiniLineParameter(4)
    }
}