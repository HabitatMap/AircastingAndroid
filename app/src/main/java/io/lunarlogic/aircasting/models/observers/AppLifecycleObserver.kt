package io.lunarlogic.aircasting.models.observers

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.lunarlogic.aircasting.events.AppToBackgroundEvent
import io.lunarlogic.aircasting.events.AppToForegroundEvent
import org.greenrobot.eventbus.EventBus

class AppLifecycleObserver : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        EventBus.getDefault().post(AppToForegroundEvent())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        EventBus.getDefault().post(AppToBackgroundEvent())
    }
}


