package pl.llp.aircasting.data.api

import android.util.Base64OutputStream
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.lang.reflect.Type
import java.util.zip.GZIPOutputStream

class GzippedParams {
    companion object {
        private val gson = Gson().newBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create()

        fun get(params: Any, paramsClass: Type): String {
            val byteStream = ByteArrayOutputStream()
            val base64OutputStream = Base64OutputStream(byteStream, 0)
            val gzip = GZIPOutputStream(base64OutputStream)
            val writer = OutputStreamWriter(gzip)
            gson!!.toJson(params, paramsClass, writer)

            writer.flush()
            gzip.finish()
            writer.close()

            return String(byteStream.toByteArray())
        }
    }
}
