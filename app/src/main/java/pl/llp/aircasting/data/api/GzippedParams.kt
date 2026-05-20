package pl.llp.aircasting.data.api

import android.util.Base64OutputStream
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.zip.GZIPOutputStream

class GzippedParams {
    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

        private val gson = GsonBuilder()
            .registerTypeAdapter(
                Date::class.java,
                JsonSerializer<Date> { src, _, _ ->
                    val formatter = SimpleDateFormat(DATE_FORMAT, Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    JsonPrimitive(formatter.format(src))
                }
            )
            .create()

        fun get(params: Any, paramsClass: Type): String {
            val byteStream = ByteArrayOutputStream()
            val base64OutputStream = Base64OutputStream(byteStream, 0)
            val gzip = GZIPOutputStream(base64OutputStream)
            val writer = OutputStreamWriter(gzip)
            gson.toJson(params, paramsClass, writer)

            writer.flush()
            gzip.finish()
            writer.close()

            return String(byteStream.toByteArray())
        }
    }
}
