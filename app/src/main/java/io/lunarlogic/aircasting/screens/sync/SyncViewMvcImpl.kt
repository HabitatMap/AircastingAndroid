package io.lunarlogic.aircasting.screens.sync

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseViewMvc

class SyncViewMvcImpl : BaseViewMvc, SyncViewMvc {
    constructor(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): super() {
        this.rootView = inflater.inflate(R.layout.activity_sync, parent, false)
    }
}
