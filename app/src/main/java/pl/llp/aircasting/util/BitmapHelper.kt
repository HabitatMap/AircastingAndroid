package pl.llp.aircasting.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class BitmapHelper {
    companion object {
        fun bitmapFromVector(context: Context?, id: Int, color: Int? = null): BitmapDescriptor? {
            if (context == null) {
                return null
            }
            val vectorDrawable =
                ResourcesCompat.getDrawable(context.resources, id, null)!!
            val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)

            if (color != null) {
                vectorDrawable.setTint(color)
            }

            vectorDrawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}
