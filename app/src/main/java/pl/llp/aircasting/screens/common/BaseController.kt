package pl.llp.aircasting.screens.common

abstract class BaseController<ViewType>(
    protected var mViewMvc: ViewType?
) {
    open fun onDestroy() {
        mViewMvc = null
    }
}
