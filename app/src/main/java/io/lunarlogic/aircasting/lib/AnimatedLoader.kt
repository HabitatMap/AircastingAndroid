package io.lunarlogic.aircasting.lib

import android.graphics.drawable.Animatable
import android.widget.ImageView

class AnimatedLoader(private val mLoader: ImageView?) {
    private var animatable: Animatable? = null

    init {
        animatable = mLoader?.drawable as? Animatable
    }

    fun start() {
        animatable?.start()
    }
}
