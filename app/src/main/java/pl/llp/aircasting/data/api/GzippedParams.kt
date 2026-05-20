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
import java.util.zip.GZIPOutputStream

class GzippedParams {
    companion object {
        // BE stores timestamps via skip_time_zone_conversion_for_attributes — the wall-clock
        // numerals are persisted as-is, and the trailing "Z" is a literal suffix, not a real
        // UTC tag. Format Dates in the phone's local TZ so uploaded numerals match what BE
        // expects (and what the V2 binary ingester writes via to_local_as_utc).
        private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

        private val gson = GsonBuilder()
            .registerTypeAdapter(
                Date::class.java,
                JsonSerializer<Date> { src, _, _ ->
                    val formatter = SimpleDateFormat(DATE_FORMAT, Locale.US)
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
