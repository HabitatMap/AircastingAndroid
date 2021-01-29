package io.lunarlogic.aircasting.lib

import android.content.Context
import org.greenrobot.eventbus.EventBus

fun Context.isSdCardSyncEnabled(): Boolean {
    val metaData = applicationInfo.metaData
    return metaData.getBoolean("sd_card_sync_enabled")
}

fun EventBus.safeRegister(subscriber: Any) {
    if (!EventBus.getDefault().isRegistered(subscriber)) {
        EventBus.getDefault().register(subscriber)
    }
}
