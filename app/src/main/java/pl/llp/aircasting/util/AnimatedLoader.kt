package pl.llp.aircasting.util

import android.graphics.drawable.Animatable
import android.widget.ImageView

class AnimatedLoader(mLoader: ImageView?) {
    private var animatable: Animatable? = null

    init {
        animatable = mLoader?.drawable as? Animatable
    }

    fun start() {
        animatable?.start()
    }

    fun stop(){
        animatable?.stop()
    }
}
