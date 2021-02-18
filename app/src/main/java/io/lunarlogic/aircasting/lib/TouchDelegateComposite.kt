package io.lunarlogic.aircasting.lib

import android.graphics.Rect
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View


class TouchDelegateComposite(view: View?) : TouchDelegate(emptyRect, view) {
    private val delegates: MutableList<TouchDelegate> =
        ArrayList()

    fun addDelegate(delegate: TouchDelegate?) {
        if (delegate != null) {
            delegates.add(delegate)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var res = false
        val x = event.x
        val y = event.y
        for (delegate in delegates) {
            event.setLocation(x, y)
            res = delegate.onTouchEvent(event) || res
        }
        return res
    }

    companion object {
        private val emptyRect: Rect = Rect()
    }
}
