package pl.llp.aircasting.screens.common

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

class CustomScrollView : ScrollView {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.action
        when (action) {
            MotionEvent.ACTION_DOWN ->
                super.onTouchEvent(ev)
            MotionEvent.ACTION_MOVE -> return false // redirect MotionEvents to ourself
            MotionEvent.ACTION_CANCEL ->
                super.onTouchEvent(ev)
            MotionEvent.ACTION_UP ->
                return false
            else -> {
            }
        }
        return false
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        super.onTouchEvent(ev)
        return true
    }
}
