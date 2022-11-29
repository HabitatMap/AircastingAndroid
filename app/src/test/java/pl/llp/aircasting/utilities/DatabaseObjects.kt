package pl.llp.aircasting.utilities

import pl.llp.aircasting.data.local.entity.MeasurementDBObject
import pl.llp.aircasting.data.local.entity.MeasurementStreamDBObject
import pl.llp.aircasting.data.local.entity.NoteDBObject
import pl.llp.aircasting.data.local.entity.SessionDBObject
import pl.llp.aircasting.data.model.Session
import pl.llp.aircasting.ui.view.screens.new_session.select_device.DeviceItem
import java.util.*

object DatabaseObjects {
    val measurementDBObject = MeasurementDBObject(
        1L,
        1L,
        1.0,
        Date(),
        1.0,
        1.0,
        1
    )
    val measurementStreamDBObject = MeasurementStreamDBObject(
        1L,
        "SENSOR_PACKAGE_NAME",
        "SENSOR_NAME",
        "MEASUREMENT_TYPE",
        "MEASUREMENT_SHORT_TYPE",
        "UNIT_NAME",
        "UNIT_SYMBOL",
        15,
        30,
        70,
        100,
        120,
        false
    )
    val sessionDBObject = SessionDBObject(
        "UUID",
        Session.Type.FIXED,
        "DEVICE_ID",
        DeviceItem.Type.AIRBEAM3,
        "NAME",
        startTime = Date(),
        endTime = null,
        latitude = 1.0,
        longitude = 1.0
    )
    val noteDBObject = NoteDBObject(
        1L,
        Date(),
        "TEXT",
        1.0,
        1.0,
        0,
        "PHOTO_LOCATION"
    )
}