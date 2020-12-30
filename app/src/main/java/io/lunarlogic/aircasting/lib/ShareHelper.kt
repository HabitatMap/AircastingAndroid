package io.lunarlogic.aircasting.lib

import android.app.Activity
import android.provider.ContactsContract
import io.lunarlogic.aircasting.models.Session

class ShareHelper {

    companion object{
        private val SENSOR_PARAM = "?sensor_name="
        private val SHARE_TEXT = "Here I send you link to my session\n"

        fun shareLink(session: Session, selectedSensorName: String): String {
            val sessionLink: String = session.urlLocation.toString() + SENSOR_PARAM + selectedSensorName
            return SHARE_TEXT + sessionLink
        }
    }

}
