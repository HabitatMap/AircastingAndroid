package pl.llp.aircasting.screens.common

import pl.llp.aircasting.lib.AppBar

abstract class BaseController<ViewType>(
    protected var mViewMvc: ViewType?
) {
    open fun onResume() {
        AppBar.adjustMenuVisibility(false)
    }

    open fun onDestroy() {
        mViewMvc = null
    }
}
