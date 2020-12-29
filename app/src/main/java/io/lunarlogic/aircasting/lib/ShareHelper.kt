package io.lunarlogic.aircasting.lib

import android.app.Activity
import android.provider.ContactsContract
import io.lunarlogic.aircasting.models.Session

class ShareHelper {

    private val SENSOR_PARAM = "?sensor_name="

    fun shareLink(activity: Activity?, session: Session, selectedSensorName: CharSequence) {
//        val sessionLink: String =
//            session.getLocation().toString() + SENSOR_PARAM + selectedSensorName
//        val text: String = kotlin.String.format(sessionLinkTemplate, sessionLink)sessionLinkTemplate
//        ContactsContract.Intents.share(activity, shareLink, shareTitle, text)
    }
}
