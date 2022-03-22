package pl.llp.aircasting.models.observers

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.greenrobot.eventbus.EventBus
import pl.llp.aircasting.events.AppToBackgroundEvent
import pl.llp.aircasting.events.AppToForegroundEvent

class AppLifecycleObserver : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        EventBus.getDefault().post(AppToForegroundEvent())
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        EventBus.getDefault().post(AppToBackgroundEvent())
    }
}


