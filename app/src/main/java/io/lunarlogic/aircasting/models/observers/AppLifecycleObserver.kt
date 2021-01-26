package io.lunarlogic.aircasting.models.observers

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class AppLifecycleObserver : LifecycleObserver {
    interface Listener {
        fun onAppToForeground()
        fun onAppToBackground()
    }

    private var mListener: Listener? = null

    fun registerListener(listener: Listener) {
        mListener = listener
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        mListener?.onAppToForeground()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        mListener?.onAppToBackground()
    }
}


