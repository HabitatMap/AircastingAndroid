package io.lunarlogic.aircasting.lib

import android.content.Context

fun Context.isSdCardSyncEnabled(): Boolean {
    val metaData = applicationInfo.metaData
    return metaData.getBoolean("sd_card_sync_enabled")
}
