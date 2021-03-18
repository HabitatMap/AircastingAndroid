package io.lunarlogic.aircasting.screens.new_session.choose_location

import io.lunarlogic.aircasting.screens.common.BaseController

class ChooseLocationController (
    private var mViewMvc: ChooseLocationViewMvc?
) : BaseController(mView = mViewMvc){

    fun registerListener(listener: ChooseLocationViewMvc.Listener) {
        mViewMvc?.registerListener(listener)
    }

    fun unregisterListener(listener: ChooseLocationViewMvc.Listener) {
        mViewMvc?.unregisterListener(listener)
    }

}
