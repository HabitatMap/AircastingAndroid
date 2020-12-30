package io.lunarlogic.aircasting.lib

import android.app.Activity
import android.provider.ContactsContract
import io.lunarlogic.aircasting.models.Session

class ShareHelper {

    companion object{
        private val SENSOR_PARAM = "?sensor_name="

        fun shareLink(activity: Activity?, session: Session, selectedSensorName: String): String {
            val sessionLink: String = session.location.toString() + SENSOR_PARAM + selectedSensorName  //todo: location has to be changed
            val text =  "Here I send you link to my session\n"      //todo: refactor to be done
//        val text: String = kotlin.String.format(sessionLinkTemplate, sessionLink)sessionLinkTemplate
//        ContactsContract.Intents.share(activity, shareLink, shareTitle, text)
            return text + sessionLink
        }
    }

}
