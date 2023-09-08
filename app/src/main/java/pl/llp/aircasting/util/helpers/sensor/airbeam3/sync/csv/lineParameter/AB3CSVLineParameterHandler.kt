package pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.lineParameter

import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.CSVMeasurement
import pl.llp.aircasting.util.helpers.sensor.airbeam3.sync.csv.CSVMeasurementStream
import pl.llp.aircasting.util.helpers.services.AveragingWindow
import javax.inject.Inject

class AB3CSVLineParameterHandler @Inject constructor() : CSVLineParameterHandler() {
    override val timeParameter: ABLineParameter = AB3LineParameter.Time
    override val dateParameter: ABLineParameter = AB3LineParameter.Date
    override val uuidParameter: ABLineParameter = AB3LineParameter.UUID
    override val deviceType: DeviceItem.Type = DeviceItem.Type.AIRBEAM3

    override val supportedStreams: Map<ABLineParameter, CSVMeasurementStream> = hashMapOf(
        AB3LineParameter.F to CSVMeasurementStream(
            "${CSVMeasurementStream.DEVICE_NAME}-F",
            "Temperature",
            "F",
            "fahrenheit",
            "F",
            15,
            45,
            75,
            105,
            135
        ),
        AB3LineParameter.RH to CSVMeasurementStream(
            "${CSVMeasurementStream.DEVICE_NAME}-RH",
            "Humidity",
            "RH",
            "percent",
            "%",
            0,
            25,
            50,
            75,
            100
        ),
        AB3LineParameter.PM1 to CSVMeasurementStream(
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
        AB3LineParameter.PM2_5 to CSVMeasurementStream(
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
        AB3LineParameter.PM10 to CSVMeasurementStream(
            "${CSVMeasurementStream.DEVICE_NAME}-PM10",
            PM_MEASUREMENT_TYPE,
            PM_MEASUREMENT_SHORT_TYPE,
            PM_UNIT_NAME,
            PM_UNIT_SYMBOL,
            0,
            20,
            50,
            100,
            200
        )
    )

    override fun getCsvMeasurement(
        line: String,
        currentStreamLineParameter: ABLineParameter,
        finalAveragingWindow: AveragingWindow
    ): CSVMeasurement? {
        val params = lineParameters(line)
        val csvMeasurement = super.getCsvMeasurement(line, currentStreamLineParameter, finalAveragingWindow)
        csvMeasurement?.latitude = getValueFor(params, AB3LineParameter.Latitude)
        csvMeasurement?.longitude = getValueFor(params, AB3LineParameter.Longitude)

        return csvMeasurement
    }

    sealed class AB3LineParameter(override val position: Int) : ABLineParameter(position) {
        object Index : AB3LineParameter(0)
        object UUID : AB3LineParameter(1)
        object Date : AB3LineParameter(2)
        object Time : AB3LineParameter(3)
        object Latitude : AB3LineParameter(4)
        object Longitude : AB3LineParameter(5)
        object F : AB3LineParameter(6)
        object C : AB3LineParameter(7)
        object K : AB3LineParameter(8)
        object RH : AB3LineParameter(9)
        object PM1 : AB3LineParameter(10)
        object PM2_5 : AB3LineParameter(11)
        object PM10 : AB3LineParameter(12)

        companion object {
            fun fromInt(position: Int) = when (position) {
                Index.position -> Index
                UUID.position -> UUID
                Date.position -> Date
                Time.position -> Time
                Latitude.position -> Latitude
                Longitude.position -> Longitude
                F.position -> F
                C.position -> C
                K.position -> K
                RH.position -> RH
                PM1.position -> PM1
                PM2_5.position -> PM2_5
                PM10.position -> PM10
                else -> null
            }
        }
    }
}