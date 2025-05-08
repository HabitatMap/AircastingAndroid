package pl.llp.aircasting.data.model.observers

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.util.events.AppToBackgroundEvent
import pl.llp.aircasting.util.events.AppToForegroundEvent

class AppLifecycleObserver : DefaultLifecycleObserver {
    companion object {
        var isAppInForeground: Boolean = true
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        EventBus.getDefault().post(AppToForegroundEvent())
        isAppInForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        EventBus.getDefault().post(AppToBackgroundEvent())
        isAppInForeground = false
    }
}


