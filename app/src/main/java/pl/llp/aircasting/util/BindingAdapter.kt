package pl.llp.aircasting.util

import android.content.res.ColorStateList
import android.util.Log
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter

object BindingAdapter {
    @JvmStatic
    @BindingAdapter("tint")
    fun ImageView.setImageTint(@ColorInt color: Int) {
        setColorFilter(color)
        imageTintList = ColorStateList.valueOf(color)
        Log.i("ImageView", this.toString())
    }
}