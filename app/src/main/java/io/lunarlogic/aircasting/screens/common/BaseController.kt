package io.lunarlogic.aircasting.screens.common

open class BaseController(
    private var mView: ViewMvc?
) {
    open fun onDestroy() {
        mView = null
    }
}
