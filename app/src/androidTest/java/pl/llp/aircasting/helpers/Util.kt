package pl.llp.aircasting.helpers

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object Util {

    private val gson = Gson()

    fun buildJson(body: Any): String {
        return gson.toJson(body)
    }

    @Throws(IOException::class)
    fun readFile(jsonFileName: String): String {
        val inputStream = this::class.java
            .getResourceAsStream("/assets/$jsonFileName")
            ?: throw NullPointerException(
                "Have you added the local resource correctly?, "
                        + "Hint: name it as: " + jsonFileName
            )
        val stringBuilder = StringBuilder()
        var inputStreamReader: InputStreamReader? = null
        try {
            inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var character: Int = bufferedReader.read()
            while (character != -1) {
                stringBuilder.append(character.toChar())
                character = bufferedReader.read()
            }
        } catch (exception: IOException) {
            exception.printStackTrace()
        } finally {
            inputStream.close()
            inputStreamReader?.close()
        }
        return stringBuilder.toString()
    }
}

