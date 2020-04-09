package io.lunarlogic.aircasting.screens.new_session

import android.view.LayoutInflater
import android.view.ViewGroup
import io.lunarlogic.aircasting.R
import io.lunarlogic.aircasting.screens.common.BaseViewMvc

class NewSessionViewMvcImpl : BaseViewMvc, NewSessionViewMvc {
    constructor(
        inflater: LayoutInflater, parent: ViewGroup?): super() {
        this.rootView = inflater.inflate(R.layout.activity_new_session, parent, false)
    }
}