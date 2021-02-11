package io.lunarlogic.aircasting.screens.settings.clearSDCard

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseViewMvc

class ClearSdCardViewMvcImpl: BaseViewMvc, ClearSDCardViewMvc {
    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.activity_new_session, parent, false) //to be changed on clear sd card layout
    }
}
