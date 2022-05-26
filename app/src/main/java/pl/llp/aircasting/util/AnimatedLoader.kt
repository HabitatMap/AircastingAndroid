package pl.llp.aircasting.util

import android.graphics.drawable.Animatable
import android.view.View
import android.widget.ImageView

class AnimatedLoader(private val mLoader: ImageView?) {
    private var animatable: Animatable? = null

    init {
        animatable = mLoader?.drawable as? Animatable
    }

    fun start() {
        mLoader?.visibility = View.VISIBLE
        animatable?.start()
    }

    fun stop() {
        animatable?.stop()
        mLoader?.visibility = View.GONE
    }
}
