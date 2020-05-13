package io.lunarlogic.aircasting.networking

import android.util.Base64OutputStream
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.util.zip.GZIPOutputStream

class GzippedSession {
    companion object {
        private val gson = Gson().newBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create()

        fun get(sessionParams: SessionParams): String {
            val byteStream = ByteArrayOutputStream()
            val base64OutputStream = Base64OutputStream(byteStream, 0)
            val gzip = GZIPOutputStream(base64OutputStream)
            val writer = OutputStreamWriter(gzip)
            gson!!.toJson(sessionParams, SessionParams::class.java, writer)

            writer.flush()
            gzip.finish()
            writer.close()

            return String(byteStream.toByteArray())
        }
    }
}
