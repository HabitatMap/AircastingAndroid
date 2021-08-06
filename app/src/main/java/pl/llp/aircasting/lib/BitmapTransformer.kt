package pl.llp.aircasting.lib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.common.io.Closeables.closeQuietly

import pl.llp.aircasting.models.Photo
import java.io.ByteArrayOutputStream

class BitmapTransformer {
    companion object {
        private val IMAGE_MAX_SIZE = 1200.0
        val JPEG_QUALITY = 90

        fun readScaledBitmap(path: String, context: Context): Photo {
            var options = getBitmapSize(path, context)
            val size = calculateScale(options)
            options = BitmapFactory.Options()
            options.inSampleSize = size
            // todo: i have to get context here to get contentResolver
            // todo: input stream, BitmapFactory.decodeStream(inputStream)
            val inputStream = context.contentResolver.openInputStream(Uri.parse(path))
            val bitmap = BitmapFactory.decodeStream(inputStream)
//            val bitmap = BitmapFactory.decodeFile(path)
            val bytes = toBytes(bitmap)
            bitmap.recycle()
            return Photo(path, bytes)
        }

        private fun toBytes(bitmap: Bitmap): ByteArray {
            var stream: ByteArrayOutputStream? = null
            return try {
                stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
                stream.toByteArray()
            } finally {
                //closeQuietly(stream)
            }
        }

        private fun getBitmapSize(path: String, context: Context): BitmapFactory.Options {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            val inputStream = context.contentResolver.openInputStream(Uri.parse(path))
            BitmapFactory.decodeStream(inputStream) // todo: not sure if i should change it this way too
//            BitmapFactory.decodeFile(path, options)
            return options
        }

        /**
         * @param options
         * @return Calculates a power of two size that will make the image close to
         * IMAGE_MAX_SIZE
         */
        private fun calculateScale(options: BitmapFactory.Options): Int {
            var scale = 1
            if (options.outHeight > IMAGE_MAX_SIZE || options.outWidth > IMAGE_MAX_SIZE) {
                val size = Math.max(options.outHeight, options.outWidth)
                scale = Math.pow(
                    2.0,
                    Math.round(Math.log(IMAGE_MAX_SIZE / size.toDouble()) / Math.log(0.5))
                        .toDouble()
                )
                    .toInt()
            }
            return scale
        }
    }
}
