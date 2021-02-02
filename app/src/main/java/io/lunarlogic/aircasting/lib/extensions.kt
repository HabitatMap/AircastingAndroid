package io.lunarlogic.aircasting.lib

import org.greenrobot.eventbus.EventBus

fun EventBus.safeRegister(subscriber: Any) {
    if (!EventBus.getDefault().isRegistered(subscriber)) {
        EventBus.getDefault().register(subscriber)
    }
}
