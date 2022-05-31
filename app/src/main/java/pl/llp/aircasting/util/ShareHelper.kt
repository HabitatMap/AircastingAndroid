package pl.llp.aircasting.util

import android.content.Context
import pl.llp.aircasting.R
import pl.llp.aircasting.data.model.LocalSession
import java.net.URLEncoder

class ShareHelper {
    companion object{
        fun shareLink(localSession: LocalSession, selectedSensorName: String, context: Context?): String {
            val sessionLink = "${localSession.urlLocation}?${context?.getString(R.string.sensor_param)}=${URLEncoder.encode(selectedSensorName)}"
            return context?.getString(R.string.share_text) + sessionLink
        }
    }
}

