package io.lunarlogic.aircasting.screens.common

import androidx.fragment.app.Fragment

abstract class BaseFragment<ViewType : BaseViewMvc, ControllerType : BaseController<ViewType>>: Fragment() {
    protected var controller: ControllerType? = null
    protected var view: ViewType? = null

    override fun onDestroy() {
        super.onDestroy()
        view = null
        controller?.onDestroy()
        controller = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view = null
        controller?.onDestroy()
        controller = null
    }
}
