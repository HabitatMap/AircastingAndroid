package io.lunarlogic.aircasting.screens.common

open class BaseController(
    private var mView: ViewMvc?
) {
    fun onDestroy() {
        mView = null
    }
}
