package pl.llp.aircasting.ui.view.common

abstract class BaseController<ViewType>(
    protected var mViewMvc: ViewType?
) {

    open fun onDestroy() {
        mViewMvc = null
    }
}
