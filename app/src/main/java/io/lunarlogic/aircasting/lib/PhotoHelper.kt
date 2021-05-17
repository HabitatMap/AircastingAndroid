package io.lunarlogic.aircasting.lib

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.query
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream


class PhotoHelper {

    companion object {
        fun getBase64String(bitmap: Bitmap): String {

            //val bitmap = BitmapFactory.decodeFile(path.path)

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        }

        fun decodeBase64(completeImageData: String): Bitmap {

            // Incase you're storing into aws or other places where we have extension stored in the starting.
            val imageDataBytes = completeImageData.substring(completeImageData.indexOf(",") + 1)
            val stream: InputStream = ByteArrayInputStream(
                Base64.decode(
                    imageDataBytes.toByteArray(),
                    Base64.DEFAULT
                )
            )
            return BitmapFactory.decodeStream(stream)
        }
    }

}
