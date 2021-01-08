package io.lunarlogic.aircasting.lib

import android.app.Activity
import android.content.Context
import android.provider.ContactsContract
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.models.Session

class ShareHelper {
    companion object{
        fun shareLink(session: Session, selectedSensorName: String, context: Context?): String {
            val sessionLink = "${session.urlLocation}?${context?.getString(R.string.sensor_param)}=${selectedSensorName}"
            return context?.getString(R.string.share_text) + sessionLink
        }
    }
}

